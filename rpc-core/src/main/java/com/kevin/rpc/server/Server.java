package com.kevin.rpc.server;

import com.kevin.rpc.common.RpcDecoder;
import com.kevin.rpc.common.RpcEncoder;
import com.kevin.rpc.common.config.ServerConfig;
import com.kevin.rpc.common.event.RpcListenerLoader;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.filter.server.ServerFilterChain;
import com.kevin.rpc.filter.server.ServerLogFilterImpl;
import com.kevin.rpc.filter.server.ServerTokenFilterImpl;
import com.kevin.rpc.registy.URL;
import com.kevin.rpc.registy.zookeeper.ZookeeperRegister;
import com.kevin.rpc.serialize.FastJsonSerializeFactory;
import com.kevin.rpc.serialize.HessianSerializeFactory;
import com.kevin.rpc.serialize.JdkSerializeFactory;
import com.kevin.rpc.serialize.KryoSerializeFactory;
import com.kevin.rpc.server.impl.DataServiceImpl;
import com.kevin.rpc.server.impl.UserServiceImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;

import static com.kevin.rpc.common.cache.CommonServerCache.*;
import static com.kevin.rpc.common.constants.RpcConstants.*;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.server
 * @Project: Kevin-RPC
 **/
@Data
public class Server {

    public void startServerApplication() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        //初始化监听器
        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();

        //初始化序列化器
        String serverSerialize = SERVER_CONFIG.getServerSerialize();
        switch (serverSerialize) {
            case JDK_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            case FAST_JSON_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
                break;
            case HESSIAN2_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new HessianSerializeFactory();
                break;
            case KRYO_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serialize type for...." + serverSerialize);
        }

        //初始化过滤链
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        serverFilterChain.addServerFilter(new ServerLogFilterImpl());
        serverFilterChain.addServerFilter(new ServerTokenFilterImpl());
        SERVER_FILTER_CHAIN = serverFilterChain;

        this.batchExportUrl();
        bootstrap.bind(SERVER_CONFIG.getPort()).sync();
        System.out.println("========== Server start success ==========");
    }

    public void initServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(8010);
        serverConfig.setRegisterAddr("localhost:2181");
        serverConfig.setApplicationName("kevin-rpc-server");
        serverConfig.setServerSerialize("kryo");
        SERVER_CONFIG = serverConfig;
    }

    /**
     * 将服务端的具体服务都暴露到注册中心
     */
    public void batchExportUrl() {
        Thread task = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (URL url : PROVIDER_URL_SET) {
                REGISTRY_SERVICE.register(url);
            }
        });
        task.start();
    }

    public void registryService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceBean();
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class<?>[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        if (REGISTRY_SERVICE == null) {
            REGISTRY_SERVICE = new ZookeeperRegister(SERVER_CONFIG.getRegisterAddr());
        }
        //默认选择该对象的第一个实现接口
        Class<?> interfaceClass = classes[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(SERVER_CONFIG.getApplicationName());
        url.addParameter("host", CommonUtil.getIpAddress());
        url.addParameter("port", String.valueOf(SERVER_CONFIG.getPort()));
        url.addParameter("group", String.valueOf(serviceWrapper.getGroup()));
        url.addParameter("limit", String.valueOf(serviceWrapper.getLimit()));
        PROVIDER_URL_SET.add(url);
        if (CommonUtil.isNotEmpty(serviceWrapper.getServiceToken())) {
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        //初始化配置
        server.initServerConfig();

        //注册服务
        ServiceWrapper serviceWrapper1 = new ServiceWrapper(new DataServiceImpl());
        serviceWrapper1.setGroup("dev");
        serviceWrapper1.setServiceToken("token-a");
        server.registryService(serviceWrapper1);

        ServiceWrapper serviceWrapper2 = new ServiceWrapper(new UserServiceImpl());
        serviceWrapper2.setGroup("test");
        serviceWrapper2.setServiceToken("token-b");
        server.registryService(serviceWrapper2);
        //设置回调
        ServerShutdownHook.registryShutdownHook();
        //启动服务
        server.startServerApplication();
    }
}