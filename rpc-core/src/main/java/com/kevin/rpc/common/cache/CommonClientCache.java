package com.kevin.rpc.common.cache;

import com.kevin.rpc.common.ChannelFuturePollingRef;
import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.config.ClientConfig;
import com.kevin.rpc.filter.client.ClientFilterChain;
import com.kevin.rpc.registry.URL;
import com.kevin.rpc.router.Router;
import com.kevin.rpc.serialize.SerializeFactory;
import com.kevin.rpc.spi.ExtensionLoader;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.cache
 * @Project: Kevin-RPC
 * @Date: 2024/6/16 10:13
 **/
public class CommonClientCache {
    /**
     * 发送队列
     */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);
    /**
     * 保存处理结果<key:UUID,value:对象>
     */
    public static Map<String, Object> RESP_MAP = new ConcurrentHashMap<>();

    //当前Client订阅了哪些服务 URL
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    //com.test.service -> <<ip:host,urlString>,<ip:host,urlString>,<ip:host,urlString>>
    public static Map<String, Map<String, String>> URL_MAP = new ConcurrentHashMap<>();
    //记录所有服务提供者的ip和端口
    public static Set<String> SERVER_ADDRESS = new HashSet<>();

    //保存服务端的路由
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();

    //路由组件
    public static Router ROUTER;
    //客户端序列化工厂
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;
    //客户但过滤链
    public static ClientFilterChain CLIENT_FILTER_CHAIN;
    //客户端配置类
    public static ClientConfig CLIENT_CONFIG;
    //SPI加载组件
    public static ExtensionLoader EXTENSION_LOADER = new ExtensionLoader();
}