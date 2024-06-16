package com.kevin.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/16 9:05
 **/
public class Server {
    private void startServerApplication() throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new StringDecoder());
                socketChannel.pipeline().addLast(new StringEncoder());
                socketChannel.pipeline().addLast(new ServerHandler());
            }
        });

        serverBootstrap.bind(8010).sync();
        System.out.println("========== Server start success ==========");
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.startServerApplication();
    }
}
