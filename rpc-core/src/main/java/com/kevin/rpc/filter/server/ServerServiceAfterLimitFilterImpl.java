package com.kevin.rpc.filter.server;

import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.ServerServiceSemaphoreWrapper;
import com.kevin.rpc.common.annotations.SPI;
import com.kevin.rpc.filter.ServerFilter;

import static com.kevin.rpc.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/25
 * @Description: 服务端用于释放semaphore对象
 **/
@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        if (!SERVER_SERVICE_SEMAPHORE_MAP.containsKey(serviceName)) {
            return;
        }
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }
}