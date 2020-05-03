package com.lmqrpc.register;


import com.alibaba.fastjson.JSON;
import com.lmqrpc.entity.ReServiceConsumer;
import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.invokerservice.ConsumerRegister;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RegisterHandlerZk implements  RegisterHandler , ConsumerRegister {

    private static String APPKEY_PATH = "/config_register";
    public static String PROVIDER_TYPE = "provider";
    public static String INVOKER_TYPE = "consumer";

    //zk client
    private static volatile ZkClient zkClient = null;

    //ZK服务地址
    private static String zkService ="127.0.0.1:2181";
    //ZK session超时时间
    private static int zkSessionTimeout=6000;
    //ZK connection超时时间
    private static int zkConnectionTimeout=7000;

    final public static Logger log= LoggerFactory.getLogger(RegisterHandlerZk.class);
    //服务提供者列表,Key:服务提供者接口  value:服务提供者服务方法列表
    private static  ConcurrentHashMap<String, List<ReServiceProvider>> providerServiceMap = new ConcurrentHashMap<String, List<ReServiceProvider>>();
    //服务端ZK服务元信息,选择服务(第一次直接从ZK拉取,后续由ZK的监听机制主动更新)
    private static final ConcurrentHashMap<String, List<ReServiceProvider>> serviceMetaDataMapToConsumers = new ConcurrentHashMap<String, List<ReServiceProvider>>();

    public RegisterHandlerZk(){

    }

    public ConcurrentHashMap<String, List<ReServiceProvider>>getServiceProviderListFromZk()
    {
        return  serviceMetaDataMapToConsumers;
    }
    public String register(List<ReServiceProvider> providers) {

        if (providers==null) {
            return "empty";
        }
        log.info("begin register service provider..................");

        //连接zk,注册服务
        synchronized (RegisterHandlerZk.class) {
            for (ReServiceProvider provider : providers) {
                String serviceClasssKey = provider.getTargetClass().getName();

                List<ReServiceProvider> providerslist = providerServiceMap.get(serviceClasssKey);
                if (providerslist == null) {
                    providerslist = new ArrayList();
                }
                providerslist.add(provider);
                providerServiceMap.put(serviceClasssKey, providerslist);
            }

            if (zkClient == null) {
                zkClient = new ZkClient(zkService, zkSessionTimeout, zkConnectionTimeout, new SerializableSerializer());
            }

            //创建 ZK命名空间/当前部署应用APP命名空间/
            String APP_KEY = providers.get(0).getServicekey();
            String ZK_PATH = APPKEY_PATH + "/" + APP_KEY;
            boolean exist = zkClient.exists(ZK_PATH);
            if (!exist) {
                zkClient.createPersistent(ZK_PATH, true);
            }

            for (Map.Entry<String, List<ReServiceProvider>> entry : providerServiceMap.entrySet()) {
                //服务分组
                String groupName = entry.getValue().get(0).getServiceGroupName();
                //创建服务提供者
                String serviceNode = entry.getKey();
                String servicePath = ZK_PATH + "/" + groupName + "/" + serviceNode + "/" + PROVIDER_TYPE;
                exist = zkClient.exists(servicePath);
                if (!exist) {
                    zkClient.createPersistent(servicePath, true);
                }

                //创建当前服务器节点
                int serverPort = entry.getValue().get(0).getProviderPort();//服务端口
                int weight = entry.getValue().get(0).getWeight();//服务权重
                int workerThreads = entry.getValue().get(0).getWorkerThreads();//服务工作线程
                String localIp = zkService;
                String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weight + "|" + workerThreads + "|" + groupName;
                exist = zkClient.exists(currentServiceIpNode);
                if (!exist) {
                    //注意,这里创建的是临时节点
                    zkClient.createEphemeral(currentServiceIpNode);
                    log.info("begin register service provider..................");
                }

                //监听注册服务的变化,同时更新数据到本地缓存
                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {

                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        if (currentChilds == null) {
                            currentChilds = new ArrayList();
                        }

                        //存活的服务IP列表
                        List<String> activityServiceIpList =  new ArrayList();
                        for(int i=0;i<currentChilds.size();i++)
                        {
                            activityServiceIpList.add(currentChilds.get(i).split("|")[0]);
                        }

                     refreshActiveService(activityServiceIpList);
                        log.info(" register service provider success..................");
                    }
                });

            }
        }
        return null;
    }

    public List<ReServiceProvider> getRegisterList(String serviceKey) {
        return providerServiceMap.get(serviceKey);
    }


    //利用ZK回调函数自动刷新当前存活的服务提供者列表数据
    private void refreshActiveService(List<String> serviceIpList) {
        if (serviceIpList == null) {
            serviceIpList = new ArrayList();
        }

        Map<String, List<ReServiceProvider>> currentServiceMetaDataMap = new ConcurrentHashMap<String, List<ReServiceProvider>>();;
        for (Map.Entry<String, List<ReServiceProvider>> entry : providerServiceMap.entrySet()) {
            String key = entry.getKey();
            List<ReServiceProvider> providerServices = entry.getValue();

            List<ReServiceProvider> serviceMetaDataModelList = currentServiceMetaDataMap.get(key);
            if (serviceMetaDataModelList == null) {
                serviceMetaDataModelList = new ArrayList();
            }

            for (ReServiceProvider serviceMetaData : providerServices) {
                if (serviceIpList.contains(serviceMetaData.getProviderIp())) {//list 判断
                    serviceMetaDataModelList.add(serviceMetaData);
                }
            }
            currentServiceMetaDataMap.put(key, serviceMetaDataModelList);
        }
        providerServiceMap.clear();
        log.info("currentServiceMetaDataMap,"+ JSON.toJSONString(currentServiceMetaDataMap));
        providerServiceMap.putAll(currentServiceMetaDataMap);
    }

    public void registerInvoker(ReServiceConsumer invoker) {
        if (invoker == null) {
            return;
        }

        //连接zk,注册服务
        synchronized (RegisterHandlerZk.class) {

            if (zkClient == null) {
                zkClient = new ZkClient(zkService, zkSessionTimeout, zkConnectionTimeout, new SerializableSerializer());
            }
            //创建 ZK命名空间/当前部署应用APP命名空间/
            boolean exist = zkClient.exists(APPKEY_PATH);
            if (!exist) {
                zkClient.createPersistent(APPKEY_PATH, true);
            }


            //创建服务消费者节点
            String remoteAppKey = invoker.getRemoteAppKey();
            String groupName = invoker.getGroupName();
            String serviceNode = invoker.getServiceClass().getName();
            String servicePath = APPKEY_PATH + "/" + remoteAppKey + "/" + groupName + "/" + serviceNode + "/" + INVOKER_TYPE;
            exist = zkClient.exists(servicePath);
            if (!exist) {
                zkClient.createPersistent(servicePath, true);
            }

            //创建当前服务器节点
            String localIp =zkService ;
            String currentServiceIpNode = servicePath + "/" + localIp;
            exist = zkClient.exists(currentServiceIpNode);
            if (!exist) {
                //注意,这里创建的是临时节点
                zkClient.createEphemeral(currentServiceIpNode);
            }
        }

    }

    public void initServiceProviderList(String servicekey,String groupName) {

        if(serviceMetaDataMapToConsumers.size()>0)
        {
            serviceMetaDataMapToConsumers.putAll(fetchOrUpdateProviderServiceMetaData(servicekey,groupName));
        }

    }

    public List<ReServiceProvider> getServiceProviderListByServiceKeyFromZk(String ServiceKey) {
        return  providerServiceMap.get(ServiceKey);
    }

    public String registerConsumer(ReServiceConsumer reServiceConsumer) {
        if (reServiceConsumer == null) {
            return "invoker is null";
        }

        //连接zk,注册服务
        synchronized (RegisterHandlerZk.class) {

            if (zkClient == null) {
                zkClient = new ZkClient(zkService, zkSessionTimeout, zkConnectionTimeout, new SerializableSerializer());
            }
            //创建 ZK命名空间/当前部署应用APP命名空间/
            boolean exist = zkClient.exists(APPKEY_PATH);
            if (!exist) {
                zkClient.createPersistent(APPKEY_PATH, true);
            }


            //创建服务消费者节点
            String remoteAppKey = reServiceConsumer.getRemoteAppKey();
            String groupName = reServiceConsumer.getGroupName();
            String serviceNode = reServiceConsumer.getServiceClass().getName();
            String servicePath = APPKEY_PATH + "/" + remoteAppKey + "/" + groupName + "/" + serviceNode + "/" + INVOKER_TYPE;
            exist = zkClient.exists(servicePath);
            if (!exist) {
                zkClient.createPersistent(servicePath, true);
            }

            //创建当前服务器节点
            String localIp =zkService;
            String currentServiceIpNode = servicePath + "/" + localIp;
            exist = zkClient.exists(currentServiceIpNode);
            if (!exist) {
                //注意,这里创建的是临时节点,毕竟消费者可以完毕，就不用监管了
                zkClient.createEphemeral(currentServiceIpNode);
            }
        }

        return "register consumer ok!";
    }


    private Map<String, List<ReServiceProvider>> fetchOrUpdateProviderServiceMetaData(String remoteAppKey, String groupName) {
        final Map<String, List< ReServiceProvider>> providerServiceMap = new ConcurrentHashMap();
        //连接zk
        synchronized (RegisterHandlerZk.class) {
            if (zkClient == null) {
                zkClient = new ZkClient(zkService, zkSessionTimeout, zkConnectionTimeout, new SerializableSerializer());
            }
        }

        //从ZK获取服务提供者列表
        String providePath = APPKEY_PATH + "/" + remoteAppKey + "/" + groupName;
        List<String> providerServices = zkClient.getChildren(providePath);

        for (String serviceName : providerServices) {
            String servicePath = providePath + "/" + serviceName + "/" + PROVIDER_TYPE;
            List<String> ipPathList = zkClient.getChildren(servicePath);
            for (String ipPath : ipPathList) {
                String serverIp = ipPath.split("|")[0];
                String serverPort = ipPath.split("|")[1];
                int weight = Integer.parseInt(ipPath.split("|")[2]);
                int workerThreads = Integer.parseInt(ipPath.split("|")[3]);
                String group = ipPath.split("|")[4];

                List<ReServiceProvider> providerServiceList = providerServiceMap.get(serviceName);
                if (providerServiceList == null) {
                    providerServiceList = new ArrayList();
                }
                ReServiceProvider providerService = new ReServiceProvider();

                try {
                    providerService.setTargetClass(ClassUtils.getClass(serviceName));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                providerService.setProviderIp(serverIp);
                providerService.setProviderPort(Integer.parseInt(serverPort));
                providerService.setWeight(weight);
                providerService.setWorkerThreads(workerThreads);
                providerService.setServiceGroupName(group);
                providerServiceList.add(providerService);

                providerServiceMap.put(serviceName, providerServiceList);
            }

            //监听注册服务的变化,同时更新数据到本地缓存
            zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {

                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    if (currentChilds == null) {
                        currentChilds = new ArrayList();
                    }
                    List<String> activityServiceIpList = new ArrayList();
                    for (int i = 0; i < currentChilds.size(); i++) {
                        activityServiceIpList.add(currentChilds.get(i).split("|")[0]);


                    }
                    refreshServiceMetaDataMap(activityServiceIpList);
                }
            });

        }
        return providerServiceMap;

        }


       public void refreshServiceMetaDataMap(List<String> serviceIpList)
    {
            if (serviceIpList == null) {
                serviceIpList = new ArrayList();
            }

            Map<String, List<ReServiceProvider>> currentServiceMetaDataMap = new ConcurrentHashMap<String, List<ReServiceProvider>>();
            for (Map.Entry<String, List<ReServiceProvider>> entry : serviceMetaDataMapToConsumers.entrySet()) {
                String serviceItfKey = entry.getKey();
                List<ReServiceProvider> serviceList = entry.getValue();

                List<ReServiceProvider> providerServiceList = currentServiceMetaDataMap.get(serviceItfKey);
                if (providerServiceList == null) {
                    providerServiceList = new ArrayList();
                }

                for (ReServiceProvider serviceMetaData : serviceList) {
                    if (serviceIpList.contains(serviceMetaData.getProviderIp())) {
                        providerServiceList.add(serviceMetaData);
                    }
                }
                currentServiceMetaDataMap.put(serviceItfKey, providerServiceList);
            }

            serviceMetaDataMapToConsumers.clear();
            serviceMetaDataMapToConsumers.putAll(currentServiceMetaDataMap);
        }


        public static RegisterHandlerZk registerHandlerZk=new RegisterHandlerZk();

    public static RegisterHandlerZk singleton() {
        return registerHandlerZk;
    }

}
