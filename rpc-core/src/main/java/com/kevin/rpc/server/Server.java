package com.kevin.rpc.server;

import com.kevin.rpc.common.RpcDecoder;
import com.kevin.rpc.common.RpcEncoder;
import com.kevin.rpc.common.config.ServerConfig;
import com.kevin.rpc.common.event.RpcListenerLoader;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.filter.ServerFilter;
import com.kevin.rpc.filter.server.ServerFilterChain;
import com.kevin.rpc.registry.AbstractRegister;
import com.kevin.rpc.registry.RegistryService;
import com.kevin.rpc.registry.URL;
import com.kevin.rpc.serialize.SerializeFactory;
import com.kevin.rpc.server.impl.DataServiceImpl;
import com.kevin.rpc.server.impl.UserServiceImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kevin.rpc.common.cache.CommonClientCache.EXTENSION_LOADER;
import static com.kevin.rpc.common.cache.CommonServerCache.*;
import static com.kevin.rpc.common.utils.CommonUtil.initializeComponent;
import static com.kevin.rpc.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.server
 * @Project: Kevin-RPC
 **/
@Data
public class Server {

    public void startServerApplication() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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
        SERVER_SERIALIZE_FACTORY = initializeComponent(SerializeFactory.class, serverSerialize);

        //初始化过滤链
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        EXTENSION_LOADER.loadExtension(ServerFilter.class);
        LinkedHashMap<String, Class<?>> filterChainMap = EXTENSION_LOADER_CLASS_CACHE.get(ServerFilter.class.getName());
        for (Map.Entry<String, Class<?>> filterChainEntry : filterChainMap.entrySet()) {
            String filterChainKey = filterChainEntry.getKey();
            Class<?> filterChainImpl = filterChainEntry.getValue();
            if (filterChainImpl == null) {
                throw new RuntimeException("no match filterChainImpl for " + filterChainKey);
            }
            serverFilterChain.addServerFilter((ServerFilter) filterChainImpl.newInstance());
        }
        SERVER_FILTER_CHAIN = serverFilterChain;

        //初始化请求分发器
        SERVER_CHANNEL_DISPATCHER.init(SERVER_CONFIG.getServerQueueSize(), SERVER_CONFIG.getServerBizThreadNums());
        SERVER_CHANNEL_DISPATCHER.startDataConsume();

        this.batchExportUrl();
        bootstrap.bind(SERVER_CONFIG.getPort()).sync();
        System.out.println("========== Server start success ==========");
    }

    public void initServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(8010);
        serverConfig.setRegisterAddr("localhost:2181");
        serverConfig.setRegisterType("zookeeper");
        serverConfig.setApplicationName("kevin-rpc-server");
        serverConfig.setServerSerialize("kryo");
        serverConfig.setServerQueueSize(5000);
        serverConfig.setServerBizThreadNums(12);
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
            try {
                REGISTRY_SERVICE = (AbstractRegister) initializeComponent(RegistryService.class, SERVER_CONFIG.getRegisterType());
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
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

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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