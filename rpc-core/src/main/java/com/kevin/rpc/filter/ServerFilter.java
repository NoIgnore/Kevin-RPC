package com.kevin.rpc.filter;

import com.kevin.rpc.common.RpcInvocation;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 服务端过滤器
 **/
public interface ServerFilter extends Filter {

    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);
}