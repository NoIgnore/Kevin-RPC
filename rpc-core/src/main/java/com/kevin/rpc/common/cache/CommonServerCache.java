package com.kevin.rpc.common.cache;


import com.kevin.rpc.registy.RegistryService;
import com.kevin.rpc.registy.URL;
import com.kevin.rpc.serialize.SerializeFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
}
