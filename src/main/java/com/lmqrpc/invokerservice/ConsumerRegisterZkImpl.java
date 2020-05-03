package com.lmqrpc.invokerservice;

import com.lmqrpc.entity.ReServiceConsumer;
import com.lmqrpc.entity.ReServiceProvider;

import java.util.List;

public class ConsumerRegisterZkImpl implements  ConsumerRegister {



    public void registerInvoker(ReServiceConsumer invoker) {

    }

    @Override
    public void initServiceProviderList(String Servicekey, String groupName) {

    }

    public void initServiceProviderList(String Servicekey) {

    }

    public List<ReServiceProvider> getServiceProviderListByServiceKeyFromZk(String ServiceKey) {
        return null;
    }

    public String registerConsumer(ReServiceConsumer reServiceConsumer) {
        return null;
    }
}
