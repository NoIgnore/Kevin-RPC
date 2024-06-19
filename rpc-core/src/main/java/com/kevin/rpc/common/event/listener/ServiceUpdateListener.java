package com.kevin.rpc.common.event.listener;

import com.kevin.rpc.client.ConnectionHandler;
import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.common.event.RpcUpdateEvent;
import com.kevin.rpc.common.event.data.URLChangeWrapper;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.router.Selector;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kevin.rpc.common.cache.CommonClientCache.CONNECT_MAP;
import static com.kevin.rpc.common.cache.CommonClientCache.ROUTER;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.listener
 * @Project: Kevin-RPC
 **/
public class ServiceUpdateListener implements RpcListener<RpcUpdateEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdateListener.class);

    @Override
    public void callBack(Object t) {
        //获取到字节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        if (CommonUtil.isEmptyList(channelFutureWrappers)) {
            LOGGER.error("[ServiceUpdateListener] channelFutureWrappers is empty");
            return;
        }

        List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();

        Set<String> finalUrl = new HashSet<>();
        List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();

        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            //如果老的url没有，说明已经被移除了
            if (!matchProviderUrl.contains(oldServerAddress)) {
                continue;
            }

            finalChannelFutureWrappers.add(channelFutureWrapper);
            finalUrl.add(oldServerAddress);
        }

        //此时老的url已经被移除了，开始检查是否有新的url
        List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
        for (String newProviderUrl : matchProviderUrl) {
            if (!finalUrl.contains(newProviderUrl)) {
                ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                String host = newProviderUrl.split(":")[0];
                Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                channelFutureWrapper.setPort(port);
                channelFutureWrapper.setHost(host);
                ChannelFuture channelFuture = null;
                try {
                    channelFuture = ConnectionHandler.createChannelFuture(host, port);
                    channelFutureWrapper.setChannelFuture(channelFuture);
                    newChannelFutureWrapper.add(channelFutureWrapper);
                    finalUrl.add(newProviderUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
        //最终在这里更新服务cache
        CONNECT_MAP.put(urlChangeWrapper.getServiceName(), finalChannelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(urlChangeWrapper.getServiceName());
        ROUTER.refreshRouterArr(selector);
    }
}