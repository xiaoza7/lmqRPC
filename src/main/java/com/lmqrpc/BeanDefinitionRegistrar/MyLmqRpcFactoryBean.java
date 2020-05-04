package com.lmqrpc.BeanDefinitionRegistrar;


import com.lmqrpc.entity.InvokerServiceCallable;
import com.lmqrpc.entity.RcRequest;
import com.lmqrpc.entity.ReServiceConsumer;
import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.invoker.NettyConsumerPoolFactory;
import com.lmqrpc.register.RegisterHandlerZk;
import io.netty.channel.Channel;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class MyLmqRpcFactoryBean implements FactoryBean {
    Class clazz;
     String remoteAppKey;
     String groupName;
    public MyLmqRpcFactoryBean(Class clazz,String remoteAppKey,String groupName)
    {
        this.clazz=clazz;
        this.remoteAppKey=remoteAppKey;
        this.groupName=groupName;
    }


    public Object getObject() throws Exception {

        Class []clazz= new Class[]{this.clazz};
               Object  proxy=  Proxy.newProxyInstance(getClass().getClassLoader(),clazz, new InvocationHandler() {

                   private volatile boolean initflag=false;
                   NettyConsumerPoolFactory nettyConsumerPoolFactory=NettyConsumerPoolFactory.getSingleton();
                   public void initNettyProxy() throws Exception
                   {
                       //初始化netty 客户端
                       //获取服务注册中心
                       RegisterHandlerZk registerCenterConsumer = RegisterHandlerZk.singleton();
                       //初始化服务提供者列表到本地缓存
                       registerCenterConsumer.initServiceProviderList(remoteAppKey, groupName);

                       //初始化Netty Channel
                       ConcurrentHashMap<String, List<ReServiceProvider>> providerMap = registerCenterConsumer.getServiceProviderListFromZk();
                       if (providerMap.isEmpty())
                       {
                           throw new RuntimeException("service provider list is empty.");
                       }

                       nettyConsumerPoolFactory.initChannelPoolFactory(providerMap);

                       //将消费者信息注册到注册中心
                       ReServiceConsumer invoker = new ReServiceConsumer();
                     //  invoker.setServiceItf(targetInterface);
                       invoker.setRemoteAppKey(remoteAppKey);
                       invoker.setGroupName(groupName);
                       registerCenterConsumer.registerInvoker(invoker);

                       synchronized(this.getClass()){
                           initflag=true;
                       }



                   }

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                if(initflag==false)
                {
                    initNettyProxy();
                }
                System.out.println("in proxy");
                //发起异步调用，
                //假设选取第一个provider的
                 int i=0;
                 InetSocketAddress address=null;
                 ArrayBlockingQueue<Channel>queue=null;

                for (Map.Entry<InetSocketAddress, ArrayBlockingQueue<Channel>> entry : nettyConsumerPoolFactory.getchannelpoolMap().entrySet()) {
                       address=entry.getKey();
                       queue=entry.getValue();
                       if(i==0)
                       {
                           break;
                       }
                       i++;

                }
                RcRequest rcRequest=new RcRequest();
                rcRequest.setArgs(args);
                rcRequest.setTargetMethodName(method.getName());
                //仍需完善
                InvokerServiceCallable invokerServiceCallable=new InvokerServiceCallable(address,rcRequest);



                return invokerServiceCallable.call();
            }
        });
    return proxy;
    }


    public Class<?> getObjectType() {
        return null;
    }


    public boolean isSingleton() {
        return false;
    }
}
