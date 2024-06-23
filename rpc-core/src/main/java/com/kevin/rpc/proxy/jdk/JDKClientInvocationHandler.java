package com.kevin.rpc.proxy.jdk;

import com.kevin.rpc.client.RpcReferenceWrapper;
import com.kevin.rpc.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.kevin.rpc.common.cache.CommonClientCache.RESP_MAP;
import static com.kevin.rpc.common.cache.CommonClientCache.SEND_QUEUE;

public class JDKClientInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private final RpcReferenceWrapper<?> rpcReferenceWrapper;

    public JDKClientInvocationHandler(RpcReferenceWrapper<?> rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        System.out.println("-----------1-----------");
        rpcInvocation.setArgs(args);
        System.out.println(args);
        rpcInvocation.setTargetMethod(method.getName());
        System.out.println(method.getName());

        // com/kevin/rpc/client/Client.java:231
        // RpcReferenceWrapper<DataService> rpcReferenceWrapper1 = new RpcReferenceWrapper<>();
        // rpcReferenceWrapper1.setAimClass(DataService.class);
        // rpcReferenceWrapper1.setGroup("dev");
        // rpcReferenceWrapper1.setServiceToken("token-a");
        // rpcReferenceWrapper1.setUrl("192.168.31.128:8010");
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttachments());

        System.out.println("-----------2-----------");
        //注入uuid，对每一次的请求都做单独区分
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        //将请求的参数放入到发送队列中
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