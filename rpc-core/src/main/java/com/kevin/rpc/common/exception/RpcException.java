package com.kevin.rpc.common.exception;

import com.kevin.rpc.common.RpcInvocation;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.exception
 * @Project: Kevin-RPC
 * @Date: 2024/6/25
 * @Description: 自定义RPC异常
 **/
public class RpcException extends RuntimeException {

    @Getter
    @Setter
    private RpcInvocation rpcInvocation;

    public RpcException(RpcInvocation rpcInvocation) {
        this.rpcInvocation = rpcInvocation;
    }

}