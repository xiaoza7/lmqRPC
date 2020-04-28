package com.lmqrpc.entity;

import com.lmqrpc.invoker.NettyConsumerPoolFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class InvokerServiceCallable implements Callable<RcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(InvokerServiceCallable.class);

    private Channel channel;
    private InetSocketAddress inetSocketAddress;
    private RcRequest request;

    public static InvokerServiceCallable of(InetSocketAddress inetSocketAddress, RcRequest request) {
        return new InvokerServiceCallable(inetSocketAddress, request);
    }


    public InvokerServiceCallable(InetSocketAddress inetSocketAddress, RcRequest request) {
        this.inetSocketAddress = inetSocketAddress;
        this.request = request;
    }
    public RcResponse call() throws Exception {
        InvokerResponseHolder.initResponseData(request.getUniqueId());
        //根据本地调用服务提供者地址获取对应的Netty通道channel队列
        ArrayBlockingQueue<Channel> blockingQueue = NettyConsumerPoolFactory.getSingleton().acquire(inetSocketAddress);
        try {
            if (channel == null) {
                //从队列中获取本次调用的Netty通道channel
                channel = blockingQueue.poll(request.getTimeout(), TimeUnit.MILLISECONDS);
            }

            //若获取的channel通道已经不可用,则重新获取一个
            while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                logger.warn("----------retry get new Channel------------");
                channel = blockingQueue.poll(request.getTimeout(), TimeUnit.MILLISECONDS);
                if (channel == null) {
                    //若队列中没有可用的Channel,则重新注册一个Channel
                    channel = NettyConsumerPoolFactory.getSingleton().registerChannel(inetSocketAddress);
                }
            }

            //将本次调用的信息写入Netty通道,发起异步调用
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();

            //从返回结果容器中获取返回结果,同时设置等待超时时间为invokeTimeout
            long invokeTimeout = request.getTimeout();
            return InvokerResponseHolder.getValue(request.getUniqueId(), invokeTimeout);
        } catch (Exception e) {
            logger.error("service invoke error.", e);
        } finally {
            //本次调用完毕后,将Netty的通道channel重新释放到队列中,以便下次调用复用
            NettyConsumerPoolFactory.getSingleton().releaseChannel(blockingQueue, channel, inetSocketAddress);
        }
        return null;
    }
}
