package com.kevin.rpc.common.event.listener;

import com.kevin.rpc.common.event.RpcDestroyEvent;
import com.kevin.rpc.registry.URL;

import static com.kevin.rpc.common.cache.CommonServerCache.PROVIDER_URL_SET;
import static com.kevin.rpc.common.cache.CommonServerCache.REGISTRY_SERVICE;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.event.listener
 * @Project: Kevin-RPC
 **/
public class ServiceDestroyListener implements RpcListener<RpcDestroyEvent> {

    @Override
    public void callBack(Object t) {
        for (URL url : PROVIDER_URL_SET) {
            REGISTRY_SERVICE.unRegister(url);
        }
    }
}