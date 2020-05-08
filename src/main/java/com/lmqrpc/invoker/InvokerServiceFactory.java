package com.lmqrpc.invoker;

import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.register.RegisterHandlerZk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * lmq
 *
 * consumer proxy factory
 */
public class InvokerServiceFactory {

    private ExecutorService fixedThreadPool = null;

    //目标服务接口
    private Class<?> targetInterface;
    //超时时间
    private int consumeTimeout;
    //消费者线程数
    private static int threadWorkerNumber = 15;
    //负载均衡
    private String loadStrategy;

   //构造
    public InvokerServiceFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.loadStrategy = clusterStrategy;
    }

    public Object invokemethod(Object proxy, Method method, Object[] args) throws Throwable {
        //服务接口名称
        String serviceKey = targetInterface.getName();
        //获取某个接口的服务提供者列表
        RegisterHandlerZk registerCenterForConsumer = RegisterHandlerZk.singleton();
        List<ReServiceProvider> providerServices = registerCenterForConsumer.getServiceProviderListByServiceKeyFromZk(serviceKey);

        return null;
    }


    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return invokemethod(proxy,method,args);
            }
        });
    }

    //创建单例InvokerServiceFactory
    private static volatile InvokerServiceFactory singleton;

    public static InvokerServiceFactory singleton(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) throws Exception {
        if (null == singleton) {
            synchronized (InvokerServiceFactory.class) {
                if (null == singleton) {
                    singleton = new InvokerServiceFactory(targetInterface, consumeTimeout, clusterStrategy);
                }
            }
        }
        return singleton;
    }




}
