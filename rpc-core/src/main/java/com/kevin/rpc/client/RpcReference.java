package com.kevin.rpc.client;

import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.proxy.ProxyFactory;

import static com.kevin.rpc.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.client
 * @Project: Kevin-RPC
 * @Description: Rpc远程调用类
 **/
public class RpcReference {

    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 根据接口类型获取代理对象
     */
    public <T> T get(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        initGlobalRpcReferenceConfig(rpcReferenceWrapper);
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }

    private void initGlobalRpcReferenceConfig(RpcReferenceWrapper<?> rpcReferenceWrapper) {
        if (CommonUtil.isEmpty(rpcReferenceWrapper.getTimeOut())) {
            rpcReferenceWrapper.setTimeOut(CLIENT_CONFIG.getTimeOut());
        }
    }
}