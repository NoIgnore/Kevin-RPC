package com.kevin.rpc.client;

import com.kevin.rpc.common.RpcInvocation;
import com.kevin.rpc.common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import static com.kevin.rpc.common.cache.CommonClientCache.CLIENT_SERIALIZE_FACTORY;
import static com.kevin.rpc.common.cache.CommonClientCache.RESP_MAP;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/15 23:16
 **/
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        RpcInvocation rpcInvocation = CLIENT_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
        //通过之前发送的uuid来注入匹配的响应数值
        if (!RESP_MAP.containsKey(rpcInvocation.getUuid())) {
            throw new IllegalArgumentException("server response is error!");
        }

        RESP_MAP.put(rpcInvocation.getUuid(), rpcInvocation);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}

