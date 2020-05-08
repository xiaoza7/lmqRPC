package com.lmqrpc.BeanDefinitionRegistrar;


import com.alibaba.fastjson.JSON;
import com.lmqrpc.entity.*;
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
import java.util.UUID;
import java.util.concurrent.*;

/*
lmq
 */


public class MyLmqRpcFactoryBean implements FactoryBean {
    Class clazz;

    public MyLmqRpcFactoryBean(Class clazz)
    {
        this.clazz=clazz;

    }


    public Object getObject() throws Exception {

        final Class []clazz= new Class[]{this.clazz};
               Object  proxy=  Proxy.newProxyInstance(getClass().getClassLoader(),clazz, new InvocationHandler() {

                   private volatile boolean initflag=false;
                   NettyConsumerPoolFactory nettyConsumerPoolFactory=NettyConsumerPoolFactory.getSingleton();
                   RegisterHandlerZk registerCenterConsumer = RegisterHandlerZk.singleton();
                   String remoteAppKey;
                   String groupName;
                   private ExecutorService fixedThreadPool = null;
                   public void initNettyProxy() throws Exception
                   {
                       //根据class上面的注解获取注解属性value
                       Class c=null;
                       try {
                           c=Class.forName(clazz[0].getName());
                       }catch (Exception e)
                       {
                           e.printStackTrace();
                       }
                       LmqRPC myAnno= (LmqRPC) c.getAnnotation(LmqRPC.class);
                        remoteAppKey=myAnno.appKey();
                        groupName=myAnno.groupName();
                        System.out.println("from lmqRpc annotation, the remotekey is "+remoteAppKey+"the groupname is: "+groupName);
                       //初始化netty 客户端
                       //获取服务注册中心
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
                       invoker.setServiceClass(this.getClass());
                      // registerCenterConsumer.registerInvoker(invoker);

                       synchronized(this.getClass()){
                           initflag=true;
                       }



                   }

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                if (initflag == false) {
                    //待优化，需要把netty初始化移动到invoke之前，在代理对象实例初始化或之前完成
                    initNettyProxy();
                }

                try {
                    //构建用来发起调用的线程池
                    if (fixedThreadPool == null) {
                        synchronized (this.getClass()) {
                            if (null == fixedThreadPool) {
                                fixedThreadPool = Executors.newFixedThreadPool(10);
                            }
                        }
                    }
                    System.out.println("in proxy");
                    //发起异步调用，
                    //假设选取第一个provider的
                    int i = 0;
                    InetSocketAddress address = null;
                    ArrayBlockingQueue<Channel> queue = null;

                    //nettyConsumerPoolFactory.
                    String servicekey = clazz[0].getName();
                    System.out.println("in proxy, the servicekey is.................>" + servicekey);
                    List<ReServiceProvider> canlist = registerCenterConsumer.getRegisterList(servicekey);
                    //假设取第一个，先不考虑负载均衡
                    if (canlist.size() == 0) {
                        System.out.println("in proxy, when get the serviceprovider list ,the res list size is 0,and.................>" + servicekey);
                    }

                    ReServiceProvider reServiceProvider = canlist.get(0);
                    System.out.println("the chosed serviceprovider is ------------>:" + JSON.toJSONString(reServiceProvider));
                    RcRequest rcRequest = new RcRequest();
                    rcRequest.setProvider(reServiceProvider);
                    rcRequest.setUniqueId(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
                    rcRequest.setArgs(args);
                    rcRequest.setTargetMethodName(method.getName());
                    rcRequest.setTimeout(6000);
                    //根据服务提供者的ip,port,构建InetSocketAddress对象,标识服务提供者地址
                    String serverIp = rcRequest.getProvider().getProviderIp();
                    int serverPort = rcRequest.getProvider().getProviderPort();
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
                    //仍需完善
                    InvokerServiceCallable invokerServiceCallable = new InvokerServiceCallable(inetSocketAddress, rcRequest);
                    Future<RcResponse> responseFuture = fixedThreadPool.submit(invokerServiceCallable);
                    //获取调用的返回结果
                    RcResponse response = responseFuture.get(rcRequest.getTimeout(), TimeUnit.MILLISECONDS);
                    if (response != null) {
                        System.out.println("the final re ia-------------->"+response.getResult().toString());
                        return response.getResult();

                    }

                    //  return invokerServiceCallable.call();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
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
