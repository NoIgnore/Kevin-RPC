package com.kevin.rpc.server;

import com.kevin.rpc.common.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/24
 * @Description: Null
 **/
@Data
public class ServerChannelReadData {

    private RpcProtocol rpcProtocol;

    private ChannelHandlerContext channelHandlerContext;
}