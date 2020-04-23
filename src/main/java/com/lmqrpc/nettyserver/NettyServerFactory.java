package com.lmqrpc.nettyserver;

import com.lmqrpc.entity.RcRequest;
import com.lmqrpc.seriable.ReNettyDecoder;
import com.lmqrpc.seriable.ReNettyEncoder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lmq
 */
//这里使用netty的reactor代替原生的threadpool+动态代理

public class NettyServerFactory {


    private static final Logger log= LoggerFactory.getLogger(NettyServerFactory.class);
    private static NettyServerFactory  nettyServer=new NettyServerFactory();
    private Channel channel;

    //set the boss thread group
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void startServer(int port) {
        synchronized (NettyServerFactory.class) {

            if (bossGroup != null || workerGroup != null) {
                return;
            }

            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ReNettyDecoder(RcRequest.class));
                    socketChannel.pipeline().addLast(new ReNettyEncoder());
                    socketChannel.pipeline().addLast(new NettyProviderHandler());


                }
            });

            try {
                channel = serverBootstrap.bind(port).sync().channel();

                log.info(" the netty server is started!");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }




    }


    public void  stopServer()
    {
        if (null == channel) {
            throw new RuntimeException("Server is Stoped");
        }
        bossGroup.shutdownGracefully();//关闭
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
        log.info(" the netty server is stoped!");


    }


    private NettyServerFactory() {
    }


    public static NettyServerFactory singleton() {
        return nettyServer;
    }


}
