package com.kevin.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.client
 * @Project: Kevin-RPC
 * @Date: 2024/6/15 23:11
 **/
public class Client {
    public ChannelFuture startClientApplication() throws InterruptedException {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new StringEncoder());
                socketChannel.pipeline().addLast(new StringDecoder());
                socketChannel.pipeline().addLast(new ClientHandler());
            }
        });

        ChannelFuture channelFuture = bootstrap.connect("localhost", 8010).sync();
        System.out.println("========== Client start success ==========");
        return channelFuture;
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        ChannelFuture channelFuture = client.startClientApplication();
        for (int i = 100; i < 999; ++i) {
            Thread.sleep(1000);
            String msg = i + " : msg from client.";
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
        }
    }
}
