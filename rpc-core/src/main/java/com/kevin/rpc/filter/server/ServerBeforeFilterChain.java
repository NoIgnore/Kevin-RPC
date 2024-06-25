package com.kevin.rpc.filter.server;

import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.filter.ServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 服务端模块的前置过滤链设计
 **/
public class ServerBeforeFilterChain {

    private static List<ServerFilter> serverFilters = new ArrayList<>();

    public void addServerFilter(ServerFilter iServerFilter) {
        serverFilters.add(iServerFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation) {
        for (ServerFilter serverFilter : serverFilters) {
            serverFilter.doFilter(rpcInvocation);
        }
    }
}