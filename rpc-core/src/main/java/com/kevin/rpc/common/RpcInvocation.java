package com.kevin.rpc.common;

import lombok.Data;

import java.util.Arrays;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common
 * @Project: Kevin-RPC
 **/
@Data
public class RpcInvocation {
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


    @Override
    public String toString() {
        return "RpcInvocation{" +
                "targetMethod='" + targetMethod + '\'' +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", uuid='" + uuid + '\'' +
                ", response=" + response +
                '}';
    }
}
