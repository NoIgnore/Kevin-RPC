package com.kevin.rpc.common;

import io.netty.channel.ChannelFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.listener
 * @Project: Kevin-RPC
 * @Description: 自定义包装类，将netty建立好的ChannelFuture做了一些封装
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelFutureWrapper {

    private String host;

    private Integer port;

    private Integer weight;

    private ChannelFuture channelFuture;

    private String group;
}