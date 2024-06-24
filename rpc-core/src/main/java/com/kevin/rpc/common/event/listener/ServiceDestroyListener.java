package com.kevin.rpc.common.event.listener;

import com.kevin.rpc.common.event.RpcDestroyEvent;
import com.kevin.rpc.registry.URL;

import java.util.Iterator;

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
        Iterator<URL> urlIterator = PROVIDER_URL_SET.iterator();
        while (urlIterator.hasNext()) {
            URL url = urlIterator.next();
            urlIterator.remove();
            REGISTRY_SERVICE.unRegister(url);
        }
    }
}