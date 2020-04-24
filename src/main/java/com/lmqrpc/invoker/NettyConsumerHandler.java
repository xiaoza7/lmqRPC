package com.lmqrpc.invoker;

import com.lmqrpc.entity.RcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyConsumerHandler  extends SimpleChannelInboundHandler<RcResponse> {

    public NettyConsumerHandler() {
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RcResponse rcResponse) throws Exception {

    }


}
