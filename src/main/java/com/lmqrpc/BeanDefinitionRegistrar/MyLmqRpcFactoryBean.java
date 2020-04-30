package com.lmqrpc.BeanDefinitionRegistrar;


import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.invoker.NettyConsumerPoolFactory;
import com.lmqrpc.register.RegisterHandlerZk;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                   public void initNettyProxy()
                   {
                       //初始化netty 客户端
                       //获取服务注册中心
                       RegisterHandlerZk registerCenterConsumer = RegisterHandlerZk.singleton();
                       //初始化服务提供者列表到本地缓存
                       registerCenterConsumer.initServiceProviderList(remoteAppKey, groupName);

                       //初始化Netty Channel
                       ConcurrentHashMap<String, List<ReServiceProvider>> providerMap = registerCenterConsumer.getServiceProviderListFromZk();
                       NettyConsumerPoolFactory nettyConsumerPoolFactory=NettyConsumerPoolFactory.getSingleton();
                       nettyConsumerPoolFactory.initChannelPoolFactory(providerMap);

                       synchronized(this.getClass()){
                           initflag=true;
                       }



                   }

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                System.out.println("in proxy");


               // if (MapUtils.isEmpty(providerMap)) {
             //       throw new RuntimeException("service provider list is empty.");
             //   }
              //  NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(providerMap);

                //获取服务提供者代理对象
               // RevokerProxyBeanFactory proxyFactory = RevokerProxyBeanFactory.singleton(targetInterface, timeout, clusterStrategy);
               // this.serviceObject = proxyFactory.getProxy();

                //为了模仿mybatis执行sql
//                Method method1 =proxy.getClass().getInterfaces()[0].getMethod(method.getName(),String.class);
//                Select select =method1.getDeclaredAnnotation(Select.class);
//                System.out.println(select.value()[0]);

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
