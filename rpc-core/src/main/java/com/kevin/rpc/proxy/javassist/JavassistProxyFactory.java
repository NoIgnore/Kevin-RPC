package com.kevin.rpc.proxy.javassist;

import com.kevin.rpc.proxy.ProxyFactory;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.proxy.javassist
 * @Project: Kevin-RPC
 **/
public class JavassistProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<?> clazz) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                clazz, new JavassistInvocationHandler(clazz));
    }
}
