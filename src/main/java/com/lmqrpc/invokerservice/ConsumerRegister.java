package com.lmqrpc.invokerservice;

import com.lmqrpc.entity.ReServiceConsumer;
import com.lmqrpc.entity.ReServiceProvider;

import java.util.List;

public interface ConsumerRegister {



    /**
     * 消费端将消费者信息注册到zk
     *
     * @param invoker
     */
    public void registerInvoker(final ReServiceConsumer invoker);


    public void initServiceProviderList(String Servicekey,String groupName);


    public List<ReServiceProvider> getServiceProviderListByServiceKeyFromZk(String ServiceKey);

    public String registerConsumer(ReServiceConsumer reServiceConsumer);

}
