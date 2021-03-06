package com.lmqrpc.test;


import com.lmqrpc.invoker.NettyConsumerPoolFactory;
import com.lmqrpc.myservice.HelloService;
import io.netty.channel.Channel;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MainTestcli {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext lmqcontext = new AnnotationConfigApplicationContext(AppConfig12.class);

         HelloService helloService=(HelloService) lmqcontext.getBean("com.lmqrpc.myservice.HelloService");
         for(int i=0;i<20;i++) {
             String ss = helloService.getres("this is a test");
             System.out.println("-----------------------*****************   " + ss);
         }

       lmqcontext.close();

        ConcurrentHashMap<InetSocketAddress, ArrayBlockingQueue<Channel>> channelpoolMap=NettyConsumerPoolFactory.getSingleton().getchannelpoolMap();
        //依次关闭channel, 此时bootgrap还没关闭，为了关闭cli,暂不处理
        for(Map.Entry entry: channelpoolMap.entrySet())
        {
            ArrayBlockingQueue<Channel>qu= (ArrayBlockingQueue<Channel>) entry.getValue();
            Iterator<Channel> itr=qu.iterator();
           while(itr.hasNext())
           {
               try {
                   Channel channel=itr.next();
                   if(!channel.isActive()){
                      channel.close();
                   }
               }catch (Exception e)
               {
                   e.printStackTrace();
               }
           }



        }

       // System.exit(0);
    }


}
