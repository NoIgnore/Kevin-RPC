package com.kevin.rpc.common.exception;

import com.kevin.rpc.common.RpcInvocation;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.exception
 * @Project: Kevin-RPC
 * @Date: 2024/6/25
 * @Description: 服务端限流异常
 **/
public class MaxServiceLimitRequestException extends RpcException {

    public MaxServiceLimitRequestException(RpcInvocation rpcInvocation) {
        super(rpcInvocation);
    }
}