package com.lmqrpc.cluster.balanceImpl;

import com.lmqrpc.cluster.ClusterBalanceStrategy;
import com.lmqrpc.entity.ReServiceProvider;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class WeightRandomClusterStrategyImpl  implements ClusterBalanceStrategy {
    public ReServiceProvider selectBest(List<ReServiceProvider> providerServices) {
        List<ReServiceProvider> providerList = new ArrayList();
        for (ReServiceProvider provider : providerServices) {
            int weight = provider.getWeight();
            for (int i = 0; i < weight; i++) {

                providerList.add(provider);
            }
        }

        int MAX_LEN = providerList.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerList.get(index);
    }
}
