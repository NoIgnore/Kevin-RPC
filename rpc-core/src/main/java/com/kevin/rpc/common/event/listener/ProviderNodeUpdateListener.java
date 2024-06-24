package com.kevin.rpc.common.event.listener;

import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.event.RpcNodeUpdateEvent;
import com.kevin.rpc.common.event.data.ProviderNodeInfo;
import com.kevin.rpc.registry.URL;

import java.util.List;

import static com.kevin.rpc.common.cache.CommonClientCache.CONNECT_MAP;
import static com.kevin.rpc.common.cache.CommonClientCache.ROUTER;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.event.listener
 * @Project: Kevin-RPC
 **/
public class ProviderNodeUpdateListener implements RpcListener<RpcNodeUpdateEvent> {

    @Override
    public void callBack(Object t) {
        ProviderNodeInfo providerNodeInfo = ((ProviderNodeInfo) t);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if (address.equals(providerNodeInfo.getAddress())) {
                //修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                //更新权重
                ROUTER.updateWeight(url);
                break;
            }
        }
    }

}
