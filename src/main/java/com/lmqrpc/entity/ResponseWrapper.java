package com.lmqrpc.entity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ResponseWrapper {


    //存储返回结果的阻塞队列
    private BlockingQueue<RcResponse> responseQueue = new ArrayBlockingQueue<RcResponse>(1);
    //结果返回时间
    private long responseTime;

    public BlockingQueue<RcResponse> getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(BlockingQueue<RcResponse> responseQueue) {
        this.responseQueue = responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * 计算该返回结果是否已经过期
     *
     * @return
     */
    public boolean isExpire() {
        RcResponse response = responseQueue.peek();
        if (response == null) {
            return false;
        }

        long timeout = response.getTimeout();
        if ((System.currentTimeMillis() - responseTime) > timeout) {
            return true;
        }
        return false;
    }

    public static ResponseWrapper of() {
        return new ResponseWrapper();
    }
}
