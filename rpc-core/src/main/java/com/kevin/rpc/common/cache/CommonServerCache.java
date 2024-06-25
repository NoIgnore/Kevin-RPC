package com.kevin.rpc.common.cache;


import com.kevin.rpc.common.ServerServiceSemaphoreWrapper;
import com.kevin.rpc.common.config.ServerConfig;
import com.kevin.rpc.dispatcher.ServerChannelDispatcher;
import com.kevin.rpc.filter.server.ServerAfterFilterChain;
import com.kevin.rpc.filter.server.ServerBeforeFilterChain;
import com.kevin.rpc.registry.RegistryService;
import com.kevin.rpc.registry.URL;
import com.kevin.rpc.serialize.SerializeFactory;
import com.kevin.rpc.server.ServiceWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.cache
 * @Project: Kevin-RPC
 * @Date: 2024/6/16 10:13
 **/
public class CommonServerCache {
    /**
     * 需要注册的对象统一放在一个MAP集合中进行管理
     */
    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<>();
    /**
     * 服务提供者提供的URL
     */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
    /**
     * 注册中心：用于服务端 服务的注册url和下线
     */
    public static RegistryService REGISTRY_SERVICE;

    /**
     * 服务端序列化工厂
     */
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;
    /**
     * 服务端过滤链
     */
    public static ServerBeforeFilterChain SERVER_BEFORE_FILTER_CHAIN;
    public static ServerAfterFilterChain SERVER_AFTER_FILTER_CHAIN;

    /**
     * 服务端配置类
     */
    public static ServerConfig SERVER_CONFIG;
    /**
     * 用于过滤链的Map<ServiceName,服务端包装类>
     */
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();

    /**
     * 请求分发器
     */
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();
    /**
     * 用于服务端限流
     */
    public static final Map<String, ServerServiceSemaphoreWrapper> SERVER_SERVICE_SEMAPHORE_MAP = new ConcurrentHashMap<>(64);

}
