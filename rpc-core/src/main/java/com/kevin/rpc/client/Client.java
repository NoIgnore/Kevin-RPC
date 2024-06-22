package com.kevin.rpc.client;

import com.alibaba.fastjson.JSON;
import com.kevin.rpc.common.RpcDecoder;
import com.kevin.rpc.common.RpcEncoder;
import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.RpcProtocol;
import com.kevin.rpc.common.config.ClientConfig;
import com.kevin.rpc.common.event.RpcListenerLoader;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.interfaces.DataService;
import com.kevin.rpc.proxy.javassist.JavassistProxyFactory;
import com.kevin.rpc.proxy.jdk.JDKProxyFactory;
import com.kevin.rpc.registy.AbstractRegister;
import com.kevin.rpc.registy.URL;
import com.kevin.rpc.registy.zookeeper.ZookeeperRegister;
import com.kevin.rpc.router.RandomRouterImpl;
import com.kevin.rpc.router.RotateRouterImpl;
import com.kevin.rpc.serialize.FastJsonSerializeFactory;
import com.kevin.rpc.serialize.HessianSerializeFactory;
import com.kevin.rpc.serialize.JdkSerializeFactory;
import com.kevin.rpc.serialize.KryoSerializeFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import static com.kevin.rpc.common.cache.CommonClientCache.*;
import static com.kevin.rpc.common.constants.RpcConstants.*;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/15 23:11
 **/
public class Client {

    @Setter
    @Getter
    private ClientConfig clientConfig;

    private AbstractRegister abstractRegister;

    @Getter
    private final Bootstrap bootstrap = new Bootstrap();

    public RpcReference initClientApplication() throws InterruptedException {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //初始化管道，包含了编解码器和客户端响应类
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        //初始化连接器
        ConnectionHandler.setBootstrap(bootstrap);

        //初始化监听器
        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();

        //初始化序列化器
        String clientSerialize = clientConfig.getClientSerialize();
        switch (clientSerialize) {
            case JDK_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            case FAST_JSON_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
                break;
            case HESSIAN2_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new HessianSerializeFactory();
                break;
            case KRYO_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serialize type for" + clientSerialize);
        }

        //初始化代理工厂
        RpcReference rpcReference;
        if (JAVASSIST_PROXY_TYPE.equals(clientConfig.getProxyType())) {
            rpcReference = new RpcReference(new JavassistProxyFactory());
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }

        return rpcReference;
    }

    public void initClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegisterAddr("localhost:2181");
        clientConfig.setApplicationName("kevin-rpc-client");
        clientConfig.setProxyType("JDK");
        clientConfig.setRouterStrategy("random");
        clientConfig.setClientSerialize("kryo");
        this.setClientConfig(clientConfig);
    }

    private void initClientRouterStrategy() {
        String routerStrategy = clientConfig.getRouterStrategy();// "random"
        if (RANDOM_ROUTER_TYPE.equals(routerStrategy)) {
            ROUTER = new RandomRouterImpl();
        } else if (ROTATE_ROUTER_TYPE.equals(routerStrategy)) {
            ROUTER = new RotateRouterImpl();
        }
    }

    /**
     * 启动服务之前需要预先订阅对应的dubbo服务
     */
    public void doSubscribeService(Class<?> serviceBean) {
        if (abstractRegister == null) {
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtil.getIpAddress());
        Map<String, String> result = abstractRegister.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(), result);
        abstractRegister.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        // url.setApplicationName(clientConfig.getApplicationName());
        // url.setServiceName(serviceBean.getName());
        // url.addParameter("host", CommonUtil.getIpAddress());
        // clientSubscribeUrl为上面添加的内容
        for (URL clientSubscribeUrl : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(clientSubscribeUrl.getServiceName());
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(clientSubscribeUrl.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            URL url = new URL();
            url.setServiceName(clientSubscribeUrl.getServiceName());
            url.addParameter("providerIps", JSON.toJSONString(providerIps));
            //客户端在此新增一个订阅的功能
            abstractRegister.doAfterSubscribe(url);
        }
    }

    /**
     * 开启发送线程，专门从事将数据包发送给服务端
     */
    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob(), "ClientAsyncSendJobThread");
        asyncSendJob.start();
    }

    /**
     * 异步发送信息任务
     */
    class AsyncSendJob implements Runnable {

        public AsyncSendJob() {
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RpcInvocation data = SEND_QUEUE.take();
                    //进行序列化
                    byte[] serialize = CLIENT_SERIALIZE_FACTORY.serialize(data);
                    //将RpcInvocation封装到RpcProtocol对象中，然后发送给服务端
                    RpcProtocol rpcProtocol = new RpcProtocol(serialize);
                    //获取netty通道
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                    //netty的通道负责发送数据给服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable {

        Client client = new Client();
        //初始化配置文件
        client.initClientConfig();
        //初始化路由策略
        client.initClientRouterStrategy();
        //初始化客户端
        RpcReference rpcReference = client.initClientApplication();

        //订阅服务
        client.doSubscribeService(DataService.class);
        //建立连接
        client.doConnectServer();
        //启动客户端
        client.startClient();
        System.out.println("========== Client start success ==========");

        //生成代理对象
        DataService dataService = rpcReference.get(DataService.class);
        //调用远程方法
        List<String> list = dataService.getList();
        System.out.println(list);

        for (int i = 100; i < 999; ++i) {
            Thread.sleep(1000);
            String msg = i + ":msg from client.";
            String s = dataService.sendData(msg);
            System.out.println(i + ":" + s);
        }
        // dataService.testError();
        // dataService.testErrorV2();
    }

}