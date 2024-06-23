package com.kevin.rpc.client;

import com.kevin.rpc.proxy.ProxyFactory;

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
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }
}