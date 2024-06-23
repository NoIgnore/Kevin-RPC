package com.kevin.rpc.filter.client;


import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.kevin.rpc.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 客户端日志记录过滤链路
 **/
public class ClientLogFilterImpl implements ClientFilter {

    private final Logger logger = LoggerFactory.getLogger(ClientLogFilterImpl.class);

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        rpcInvocation.getAttachments().put("c_app_name", CLIENT_CONFIG.getApplicationName());
        logger.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -----> " +
                rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }

}