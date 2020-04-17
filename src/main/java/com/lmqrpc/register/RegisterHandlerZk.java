package com.lmqrpc.register;


import com.alibaba.fastjson.JSON;
import com.lmqrpc.entity.ReServiceProvider;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RegisterHandlerZk implements  RegisterHandler {

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

    public String register(List<ReServiceProvider> providers) {

        if (providers==null) {
            return "empty";
        }

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
                String localIp = entry.getValue().get(0).getProviderIp();
                String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weight + "|" + workerThreads + "|" + groupName;
                exist = zkClient.exists(currentServiceIpNode);
                if (!exist) {
                    //注意,这里创建的是临时节点
                    zkClient.createEphemeral(currentServiceIpNode);
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
                    }
                });

            }
        }
        return null;
    }

    public List<ReServiceProvider> getRegisterList(String serviceKey) {
        return null;
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
}
