package com.kevin.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.kevin.rpc.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common
 * @Project: Kevin-RPC
 **/
public class RpcDecoder extends ByteToMessageDecoder {
    // 魔数short2个字节 + 内容长度4个字节
    public final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            if (byteBuf.readableBytes() > MAGIC_NUMBER) {
                byteBuf.skipBytes(byteBuf.readableBytes());
            }

            int beginReader = byteBuf.readerIndex();
            byteBuf.markReaderIndex();
            if (byteBuf.readShort() != 123) {
                // 不是魔数开头，说明是非法的客户端发来的数据包
                ctx.close();
                return;
            }

            int length = byteBuf.readInt();
            //说明剩余的数据包不是完整的，这里需要重置下读索引
            if (byteBuf.readableBytes() < length) {
                byteBuf.readerIndex(beginReader);
                return;
            }
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);

            out.add(rpcProtocol);
        }
    }
}
