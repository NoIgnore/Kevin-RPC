package com.kevin.rpc.router;

import com.kevin.rpc.common.ChannelFutureWrapper;
import com.kevin.rpc.registy.URL;

public interface Router {

    /**
     * 刷新路由数组
     *
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求的连接通道
     *
     * @param selector
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重信息
     *
     * @param url
     */
    void updateWeight(URL url);
}