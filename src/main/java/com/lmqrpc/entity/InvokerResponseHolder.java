package com.lmqrpc.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InvokerResponseHolder {

    private static ConcurrentHashMap<String,ResponseWrapper>responseMap= new ConcurrentHashMap<String,ResponseWrapper>();

    private static ExecutorService deleteExpiredExecutor = Executors.newSingleThreadExecutor();

    static {
        //删除超时未获取到结果的key,防止内存泄露
//        deleteExpiredExecutor.execute(new Runnable() {
//
//            public void run() {
//                while (true) {
//                    try {
//                        for (Map.Entry<String, ResponseWrapper> entry : responseMap.entrySet()) {
//                            boolean isExpire = entry.getValue().isExpire();
//                            if (isExpire) {
//                                responseMap.remove(entry.getKey());
//                            }
//                            Thread.sleep(50);
//                        }
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        });
    }

    /**
     * 初始化返回结果容器,requestUniqueKey唯一标识本次调用
     *
     * @param requestUniqueKey
     */
    public static void initResponseData(String requestUniqueKey) {
        responseMap.put(requestUniqueKey, ResponseWrapper.of());
    }


    /**
     * 将Netty调用异步返回结果放入阻塞队列
     *
     * @param response
     */
    public static void putResultValue(RcResponse response) {
        long currentTime = System.currentTimeMillis();
        ResponseWrapper responseWrapper = responseMap.get(response.getUniqueId());
        responseWrapper.setResponseTime(currentTime);
        responseWrapper.getResponseQueue().add(response);
        responseMap.put(response.getUniqueId(), responseWrapper);
    }


    /**
     * 从阻塞队列中获取Netty异步返回的结果值
     *
     * @param requestUniqueKey
     * @param timeout
     * @return
     */
    public static RcResponse getValue(String requestUniqueKey, long timeout) {
        ResponseWrapper responseWrapper = responseMap.get(requestUniqueKey);
        try {

      // RcResponse re= responseWrapper.getResponseQueue().take();
       // return re;
          return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            responseMap.remove(requestUniqueKey);
        }
    }


}
