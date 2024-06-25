package com.kevin.rpc.filter.server;

import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.annotations.SPI;
import com.kevin.rpc.filter.ServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 服务端日志过滤器
 **/
@SPI("before")
public class ServerLogFilterImpl implements ServerFilter {

    private final Logger logger = LoggerFactory.getLogger(ServerLogFilterImpl.class);

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        logger.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -----> " +
                rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}