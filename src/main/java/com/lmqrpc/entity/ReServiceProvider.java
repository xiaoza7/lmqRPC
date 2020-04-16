package com.lmqrpc.entity;

import java.lang.reflect.Method;

/**
 * lmq
 */

//服务生产者注册类,这里以目标类及方法为注册的组合键,
public class ReServiceProvider {

 private Class<?> targetClass;

  private Method targetMethod;

  private Object providerObject;

  //the weight of this service, for circle query and load balance
  private int weight;

  private String providerIp;

  private String providerPort;

  //base on netty, and its' netty path for the provider
  private String  servicekey;

  //base on group for service
  private String serviceGroupName;

  private long timeOut;



}
