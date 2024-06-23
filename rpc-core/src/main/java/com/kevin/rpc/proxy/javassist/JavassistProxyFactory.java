package com.kevin.rpc.proxy.javassist;

import com.kevin.rpc.client.RpcReferenceWrapper;
import com.kevin.rpc.proxy.ProxyFactory;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.proxy.javassist
 * @Project: Kevin-RPC
 **/
public class JavassistProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                rpcReferenceWrapper.getAimClass(), new JavassistInvocationHandler(rpcReferenceWrapper));
    }
}
