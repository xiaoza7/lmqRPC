package com.lmqrpc.cluster.balanceImpl;

import com.lmqrpc.cluster.ClusterBalanceStrategy;
import com.lmqrpc.entity.ReServiceProvider;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

public class RandomStrategyImpl implements ClusterBalanceStrategy {
    public ReServiceProvider selectBest(List<ReServiceProvider> providerServices) {
        int rsize=providerServices.size();
        int index= RandomUtils.nextInt(0,rsize-1);

        return providerServices.get(index);

    }
}
