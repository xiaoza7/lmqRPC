package com.lmqrpc.register;

import com.lmqrpc.entity.ReServiceProvider;

import java.util.List;

public interface RegisterHandler {

    //register the serviceprovider
    public String  register(List<ReServiceProvider> providers);

    //get the provider list for the service key
    public List<ReServiceProvider> getRegisterList(String serviceKey);

}
