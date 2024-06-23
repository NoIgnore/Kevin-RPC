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
 * @Description: ip直连过滤器
 **/
public class DirectInvokeFilterImpl implements ClientFilter {

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        // com/kevin/rpc/client/Client.java:231
        // RpcReferenceWrapper<DataService> rpcReferenceWrapper1 = new RpcReferenceWrapper<>();
        // rpcReferenceWrapper1.setAimClass(DataService.class);
        // rpcReferenceWrapper1.setGroup("dev");
        // rpcReferenceWrapper1.setServiceToken("token-a");
        // rpcReferenceWrapper1.setUrl("192.168.31.128:8010");
        String url = (String) rpcInvocation.getAttachments().get("url");
        if (url == null) return;

        src.removeIf(channelFutureWrapper -> !(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()).equals(url));
        if (CommonUtil.isEmptyList(src)) {
            throw new RuntimeException("no match provider url for " + url);
        }
    }
}