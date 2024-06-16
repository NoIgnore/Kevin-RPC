package com.kevin.rpc.common;

import io.netty.channel.ChannelFuture;
import lombok.Data;

@Data
public class ChannelFutureWrapper {

    private String host;

    private Integer port;

    private ChannelFuture channelFuture;
}