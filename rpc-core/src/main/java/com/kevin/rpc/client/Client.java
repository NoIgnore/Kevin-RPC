package com.kevin.rpc.client;

import com.alibaba.fastjson.JSON;
import com.kevin.rpc.common.RpcDecoder;
import com.kevin.rpc.common.RpcEncoder;
import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.RpcProtocol;
import com.kevin.rpc.common.config.PropertiesBootstrap;
import com.kevin.rpc.common.event.RpcListenerLoader;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.filter.ClientFilter;
import com.kevin.rpc.filter.client.ClientFilterChain;
import com.kevin.rpc.interfaces.DataService;
import com.kevin.rpc.interfaces.UserService;
import com.kevin.rpc.proxy.ProxyFactory;
import com.kevin.rpc.registry.AbstractRegister;
import com.kevin.rpc.registry.RegistryService;
import com.kevin.rpc.registry.URL;
import com.kevin.rpc.router.Router;
import com.kevin.rpc.serialize.SerializeFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.Getter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.kevin.rpc.common.cache.CommonClientCache.*;
import static com.kevin.rpc.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;
import static com.kevin.rpc.common.utils.CommonUtil.initializeComponent;
import static com.kevin.rpc.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/15 23:11
 **/
public class Client {

    private AbstractRegister abstractRegister;

    @Getter
    private final Bootstrap bootstrap = new Bootstrap();

    public RpcReference initClientApplication() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                //初始化管道，包含了编解码器和客户端响应类
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(CLIENT_CONFIG.getMaxServerRespDataSize(), delimiter));
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

        // 初始化路由策略
        String routerStrategy = CLIENT_CONFIG.getRouterStrategy();
        ROUTER = initializeComponent(Router.class, routerStrategy);

        // 初始化序列化器
        String clientSerialize = CLIENT_CONFIG.getClientSerialize();
        CLIENT_SERIALIZE_FACTORY = initializeComponent(SerializeFactory.class, clientSerialize);

        //初始化过滤链
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        EXTENSION_LOADER.loadExtension(ClientFilter.class);
        LinkedHashMap<String, Class<?>> filterChainMap = EXTENSION_LOADER_CLASS_CACHE.get(ClientFilter.class.getName());
        for (Map.Entry<String, Class<?>> filterChainEntry : filterChainMap.entrySet()) {
            String filterChainKey = filterChainEntry.getKey();
            Class<?> filterChainImpl = filterChainEntry.getValue();
            if (filterChainImpl == null) {
                throw new RuntimeException("no match filterChainImpl for " + filterChainKey);
            }
            clientFilterChain.addClientFilter((ClientFilter) filterChainImpl.newInstance());
        }
        CLIENT_FILTER_CHAIN = clientFilterChain;

        //初始化代理工厂
        String proxyType = CLIENT_CONFIG.getProxyType();
        ProxyFactory factory = initializeComponent(ProxyFactory.class, proxyType);
        return new RpcReference(factory);

    }

    public void initClientConfig() {
        CLIENT_CONFIG = PropertiesBootstrap.loadClientConfigFromLocal();
    }

    /**
     * 启动服务之前需要预先订阅对应的dubbo服务
     */
    public void doSubscribeService(Class<?> serviceBean) {
        if (abstractRegister == null) {
            try {
                //初始化注册中心
                String registerType = CLIENT_CONFIG.getRegisterType();
                abstractRegister = (AbstractRegister) initializeComponent(RegistryService.class, registerType);
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
        URL url = new URL();
        url.setApplicationName(CLIENT_CONFIG.getApplicationName());
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
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data);
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

        //初始化客户端
        RpcReference rpcReference = client.initClientApplication();

        //订阅服务
        client.doSubscribeService(DataService.class);
        client.doSubscribeService(UserService.class);

        //建立连接
        client.doConnectServer();
        //启动客户端
        client.startClient();
        System.out.println("========== Client start success ==========");

        //生成代理对象DataService
        RpcReferenceWrapper<DataService> rpcReferenceWrapper1 = new RpcReferenceWrapper<>();
        rpcReferenceWrapper1.setAimClass(DataService.class);
        rpcReferenceWrapper1.setGroup("dev");
        rpcReferenceWrapper1.setServiceToken("token-a");
        rpcReferenceWrapper1.setUrl("192.168.31.128:8010");
        rpcReferenceWrapper1.setRetry(1);
        rpcReferenceWrapper1.setTimeOut(CLIENT_CONFIG.getTimeOut());
        DataService dataService = rpcReference.get(rpcReferenceWrapper1);

        // 调用远程方法
        List<String> list = dataService.getList();
        System.out.println(list);

        for (int i = 100; i < 105; ++i) {
            Thread.sleep(1000);
            String msg = i + ":msg from client.";
            String s = dataService.sendData(msg);
            System.out.println(i + ":" + s);
        }
        // dataService.testError();
        // dataService.testErrorV2();

        // 生成代理对象UserService
        RpcReferenceWrapper<UserService> rpcReferenceWrapper2 = new RpcReferenceWrapper<>();
        rpcReferenceWrapper2.setAimClass(UserService.class);
        rpcReferenceWrapper2.setGroup("test");
        rpcReferenceWrapper2.setServiceToken("token-b");
        rpcReferenceWrapper2.setRetry(3);
        rpcReferenceWrapper2.setAsync(true);
        // rpcReferenceWrapper2.setUrl("192.168.31.123:8010");
        UserService userService = rpcReference.get(rpcReferenceWrapper2);
        // 调用远程方法
        userService.test();
    }

}