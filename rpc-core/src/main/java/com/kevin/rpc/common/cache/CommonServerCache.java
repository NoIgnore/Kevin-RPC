package com.kevin.rpc.common.cache;

import java.util.HashMap;
import java.util.Map;

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
}
