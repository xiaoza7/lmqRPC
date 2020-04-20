package com.lmqrpc.cluster;

import com.lmqrpc.cluster.balanceImpl.RandomStrategyImpl;
import com.lmqrpc.cluster.balanceImpl.WeightRandomClusterStrategyImpl;

import java.util.concurrent.ConcurrentHashMap;

/**
 * lmq
 */

public class ClusterBalancedFacory {


    private static final ConcurrentHashMap<String, ClusterBalanceStrategy> clusterStrategyMap = new ConcurrentHashMap();

    static{

        clusterStrategyMap.put("Random", new RandomStrategyImpl());
        clusterStrategyMap.put("WeightRandom", new WeightRandomClusterStrategyImpl());


    }


    public static ClusterBalanceStrategy queryClusterStrategy(String clusterStrategy) {

        if (clusterStrategy == null) {
            //default is random select
            return new RandomStrategyImpl();
        }

        return clusterStrategyMap.get(clusterStrategy);
    }


}
