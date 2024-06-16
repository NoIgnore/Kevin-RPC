package com.kevin.rpc.proxy.javassist;

import com.kevin.rpc.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.kevin.rpc.common.cache.CommonClientCache.RESP_MAP;
import static com.kevin.rpc.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.proxy.javassist
 * @Project: Kevin-RPC
 **/
public class JavassistInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private final Class<?> clazz;

    public JavassistInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(clazz.getName());
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        SEND_QUEUE.add(rpcInvocation);
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < 3 * 1000) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                RESP_MAP.remove(rpcInvocation.getUuid());
                return ((RpcInvocation) object).getResponse();
            }
        }
        RESP_MAP.remove(rpcInvocation.getUuid());
        throw new TimeoutException("client wait server's response timeout!");
    }
}
