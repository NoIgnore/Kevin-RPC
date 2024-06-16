package com.kevin.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/16 9:05
 **/
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接收到的信息: " + msg.toString());
        ctx.writeAndFlush("Hello client, from server");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }
}
