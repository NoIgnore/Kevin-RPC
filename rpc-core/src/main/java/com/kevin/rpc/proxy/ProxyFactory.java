package com.kevin.rpc.proxy;

import com.kevin.rpc.client.RpcReferenceWrapper;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.proxy
 * @Project: Kevin-RPC
 * @Date: 2024/6/16 9:49
 **/
public interface ProxyFactory {
    <T> T getProxy(final RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable;
}
