package com.lmqrpc.nettyserver;

import com.lmqrpc.entity.RcRequest;
import com.lmqrpc.seriable.ReNettyDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServerFactory {

    private static NettyServerFactory  nettyServer=new NettyServerFactory();
    private Channel channel;

    //set the boss thread group
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void startServer(int port)
    {
        synchronized (NettyServerFactory.class){

            if(bossGroup!=null||workerGroup!=null)
            {
                return ;
            }

            bossGroup = new NioEventLoopGroup();
            workerGroup=new NioEventLoopGroup();
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG,1024).childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ReNettyDecoder(RcRequest.class));


                }
            });

        }


    }


}
