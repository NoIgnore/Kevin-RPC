package com.kevin.rpc.filter.server;

import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.annotations.SPI;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.filter.ServerFilter;
import com.kevin.rpc.server.ServiceWrapper;

import static com.kevin.rpc.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 简单版本的token校验
 **/
@SPI("before")
public class ServerTokenFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        if (!PROVIDER_SERVICE_WRAPPER_MAP.containsKey(rpcInvocation.getTargetServiceName())) {
            return;
        }
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if (CommonUtil.isEmpty(matchToken)) return;
        if (CommonUtil.isNotEmpty(token) && token.equals(matchToken)) return;
        throw new RuntimeException("token is " + token + " , verify result is false!");
    }
}