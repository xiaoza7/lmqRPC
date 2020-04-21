package com.lmqrpc.seriable;

import com.lmqrpc.seriable.utils.JavaDeaultseirabler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/*
自定义编码器
 */


public class ReNettyEncoder extends MessageToByteEncoder {
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        //假设是默认的java序列
        JavaDeaultseirabler deaultseirable=   new JavaDeaultseirabler();
      byte []data=deaultseirable.serialize(o);

      byteBuf.writeInt(data.length);
      byteBuf.writeBytes(data);


    }
}
