package com.kevin.rpc.filter.client;

import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.filter.ClientFilter;

import java.util.List;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.filter.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: 服务分组的过滤链路
 **/
public class GroupFilterImpl implements ClientFilter {

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        src.removeIf(channelFutureWrapper -> !channelFutureWrapper.getGroup().equals(group));
        if (CommonUtil.isEmptyList(src)) {
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}