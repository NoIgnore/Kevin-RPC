package com.kevin.rpc.filter;

import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.RpcInvocation;

import java.util.List;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 客户端过滤器
 **/
public interface ClientFilter {

    /**
     * 执行过滤链
     *
     * @param src
     * @param rpcInvocation
     * @return
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}