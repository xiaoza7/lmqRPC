package com.lmqrpc.cluster;

import com.lmqrpc.entity.ReServiceProvider;

import java.util.List;

public interface ClusterBalanceStrategy {


    public ReServiceProvider selectBest(List<ReServiceProvider> providerServices);
}
