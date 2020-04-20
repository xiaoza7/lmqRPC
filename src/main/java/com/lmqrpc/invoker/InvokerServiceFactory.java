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
        //根据软负载策略,从服务提供者列表选取本次调用的服务提供者
//        ClusterStrategy clusterStrategyService = ClusterEngine.queryClusterStrategy(clusterStrategy);
//        ProviderService providerService = clusterStrategyService.select(providerServices);
//        //复制一份服务提供者信息
//        ProviderService newProvider = providerService.copy();
//        //设置本次调用服务的方法以及接口
//        newProvider.setServiceMethod(method);
//        newProvider.setServiceItf(targetInterface);
//
//        //声明调用AresRequest对象,AresRequest表示发起一次调用所包含的信息
//        final AresRequest request = new AresRequest();
//        //设置本次调用的唯一标识
//        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
//        //设置本次调用的服务提供者信息
//        request.setProviderService(newProvider);
//        //设置本次调用的超时时间
//        request.setInvokeTimeout(consumeTimeout);
//        //设置本次调用的方法名称
//        request.setInvokedMethodName(method.getName());
//        //设置本次调用的方法参数信息
//        request.setArgs(args);
//
//        try {
//            //构建用来发起调用的线程池
//            if (fixedThreadPool == null) {
//                synchronized (RevokerProxyBeanFactory.class) {
//                    if (null == fixedThreadPool) {
//                        fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
//                    }
//                }
//            }
//            //根据服务提供者的ip,port,构建InetSocketAddress对象,标识服务提供者地址
//            String serverIp = request.getProviderService().getServerIp();
//            int serverPort = request.getProviderService().getServerPort();
//            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
//            //提交本次调用信息到线程池fixedThreadPool,发起调用
//            Future<AresResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(inetSocketAddress, request));
//            //获取调用的返回结果
//            AresResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
//            if (response != null) {
//                return response.getResult();
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
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
