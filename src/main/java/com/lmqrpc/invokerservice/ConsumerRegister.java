package com.lmqrpc.invokerservice;

import com.lmqrpc.entity.ReServiceConsumer;

public interface ConsumerRegister {



    /**
     * 消费端将消费者信息注册到zk对应的节点下
     *
     * @param invoker
     */
    public void registerInvoker(final ReServiceConsumer invoker);
}
