package com.lmqrpc.seriable;

import com.lmqrpc.seriable.utils.JavaDeaultseirabler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ReNettyDecoder extends ByteToMessageDecoder {

    JavaDeaultseirabler deaultseirable=   new JavaDeaultseirabler();
    private Class<?> targetClass;

    public ReNettyDecoder(Class<?> targetClass)
    {
        this.targetClass=targetClass;

    }


    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //同样处理tcp的粘包分包，自定义，以长度作为解决方案
      if(byteBuf.readableBytes()<4)
      {
          return;
      }
      byteBuf.markReaderIndex();//reset
         int bytelength=byteBuf.readInt();
         if(bytelength<0)
         {
             //the end,  ==-1
             channelHandlerContext.close();
         }
        if (byteBuf.readableBytes() < bytelength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[bytelength];
        byteBuf.readBytes(data);

       Object obj= deaultseirable.deserialize(data,targetClass);
        list.add(obj);





    }
}
