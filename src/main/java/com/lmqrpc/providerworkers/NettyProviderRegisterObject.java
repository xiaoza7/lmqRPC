package com.lmqrpc.providerworkers;



import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.nettyserver.NettyServerFactory;
import com.lmqrpc.register.RegisterHandler;
import com.lmqrpc.register.RegisterHandlerZk;
import com.lmqrpc.utils.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//这里以生产者netty server对外界提供服务，暂时不用普通的executorpool+代理形式
public class NettyProviderRegisterObject {
    protected final Logger logger = LoggerFactory.getLogger(NettyProviderRegisterObject.class);

    //服务接口
    private Class<?> serviceItf;
    //服务实现
    private Object serviceObject;
    //服务端口
    private int serverPort;
    //服务超时时间
    private long timeout;
    //服务代理对象,暂时没有用到
    private Object serviceProxyObject;
    //服务提供者唯一标识
    private String appKey;
    //服务分组组名
    private String groupName = "default";
    //服务提供者权重,默认为1 ,范围为[1-100]
    private int weight = 1;
    //服务端线程数,默认10个线程
    private int workerThreads = 10;

    public NettyProviderRegisterObject( Class<?> serviceItf,Object serviceObject,int serverPort,long timeout,String appKey,String groupName)
    {
        this.serviceItf=serviceItf;
        this.serverPort=serverPort;
        this.appKey=appKey;
        this.timeout=timeout;
        this.serviceObject=serviceObject;
        this.groupName=groupName;

    }

    public Object getObject() throws Exception {
        return serviceProxyObject;
    }

    public Class<?> getObjectType() {
        return serviceItf;
    }

    public boolean isSingleton() {
        return true;
    }


    public void afterPropertiesSetToRegister() throws Exception {


        //注册到zk,元数据注册中心
        List<ReServiceProvider> providerServiceList = buildProviderServiceInfos();
        RegisterHandler registerCenterForProvider = RegisterHandlerZk.singleton();
        registerCenterForProvider.register(providerServiceList);
        logger.info("afterPropertiesSetToRegister end-------------->");
        //启动Netty服务端
        NettyServerFactory.singleton().startServer(serverPort);
    }


    private List<ReServiceProvider> buildProviderServiceInfos() {
        List<ReServiceProvider> providerList =new ArrayList();
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            ReServiceProvider providerService = new ReServiceProvider();
            providerService.setTargetClass(serviceItf);
            providerService.setProviderObject(serviceObject);
            //注意这边的ip设置
            providerService.setProviderIp(IPUtils.localIp());
            providerService.setProviderPort(serverPort);
            providerService.setTimeOut(timeout);
            providerService.setTargetMethod(method);
            providerService.setWeight(weight);
            providerService.setWorkerThreads(workerThreads);
            providerService.setServicekey(appKey);
            providerService.setServiceGroupName(groupName);
            providerList.add(providerService);
        }
        return providerList;
    }


    public Class<?> getServiceItf() {
        return serviceItf;
    }

    public void setServiceItf(Class<?> serviceItf) {
        this.serviceItf = serviceItf;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Object getServiceProxyObject() {
        return serviceProxyObject;
    }

    public void setServiceProxyObject(Object serviceProxyObject) {
        this.serviceProxyObject = serviceProxyObject;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
