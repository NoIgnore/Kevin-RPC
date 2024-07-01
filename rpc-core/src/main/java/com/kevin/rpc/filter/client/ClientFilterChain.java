package com.kevin.rpc.filter.client;

import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.filter.ClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 客户端模块的过滤链设计
 **/
public class ClientFilterChain {

    private static final List<ClientFilter> clientFilterList = new ArrayList<>();

    public void addClientFilter(ClientFilter iClientFilter) {
        clientFilterList.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (ClientFilter iClientFilter : clientFilterList) {
            iClientFilter.doFilter(src, rpcInvocation);
        }
    }

}