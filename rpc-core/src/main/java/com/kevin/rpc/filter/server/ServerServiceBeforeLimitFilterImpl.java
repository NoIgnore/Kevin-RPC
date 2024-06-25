package com.kevin.rpc.filter.server;

import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.ServerServiceSemaphoreWrapper;
import com.kevin.rpc.common.annotations.SPI;
import com.kevin.rpc.common.exception.MaxServiceLimitRequestException;
import com.kevin.rpc.filter.ServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

import static com.kevin.rpc.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/25
 * @Description: 服务端方法限流过滤器
 **/
@SPI("before")
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceBeforeLimitFilterImpl.class);

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        if (!SERVER_SERVICE_SEMAPHORE_MAP.containsKey(serviceName)) {
            return;
        }
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        //从缓存中提取semaphore对象
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        boolean tryResult = semaphore.tryAcquire();
        if (!tryResult) {
            LOGGER.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException rpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(rpcException);
            throw rpcException;
        }
    }
}