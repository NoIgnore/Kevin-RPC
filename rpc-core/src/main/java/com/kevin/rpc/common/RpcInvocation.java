package com.kevin.rpc.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common
 * @Project: Kevin-RPC
 **/
@Data
public class RpcInvocation implements Serializable {

    private static final long serialVersionUID = 2951293262547830249L;

    /**
     * 请求的目标方法, 例如sendData
     */
    private String targetMethod;

    /**
     * 请求的目标服务名称, 例如：com.rpc.interfaces.DataService
     */
    private String targetServiceName;

    /**
     * 请求参数信息
     */
    private Object[] args;

    private String uuid;

    /**
     * 接口响应的数据（如果是异步调用或者void类型，这里就为空）
     */
    private Object response;

    private Map<String, Object> attachments = new ConcurrentHashMap<>();
    // setAttachments -> com/kevin/rpc/proxy/javassist/JavassistInvocationHandler.java:35
    // setAttachments -> com/kevin/rpc/proxy/jdk/JDKClientInvocationHandler.java:33

    /**
     * 记录服务端抛出的异常信息
     */
    private Throwable e;
    /**
     * 失败重试次数
     */
    private int retry;

    @Override
    public String toString() {
        return "RpcInvocation{" +
                "targetMethod='" + targetMethod + '\'' +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", uuid='" + uuid + '\'' +
                ", response=" + response +
                ", e=" + e +
                ", retry=" + retry +
                '}';
    }
}
