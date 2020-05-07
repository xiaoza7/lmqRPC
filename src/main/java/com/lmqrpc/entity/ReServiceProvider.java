package com.lmqrpc.entity;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * lmq
 */

//服务生产者注册类,这里以目标类及方法为注册的组合键,
public class ReServiceProvider implements Serializable {

 private Class<?> targetClass;

  private Method targetMethod;

  private Object providerObject;

  //the weight of this service, for circle query and load balance
  private int weight;

 public Class<?> getTargetClass() {
  return targetClass;
 }

 public void setTargetClass(Class<?> targetClass) {
  this.targetClass = targetClass;
 }

 public Method getTargetMethod() {
  return targetMethod;
 }

 public void setTargetMethod(Method targetMethod) {
  this.targetMethod = targetMethod;
 }

 public Object getProviderObject() {
  return providerObject;
 }

 public void setProviderObject(Object providerObject) {
  this.providerObject = providerObject;
 }

 public int getWeight() {
  return weight;
 }

 public void setWeight(int weight) {
  this.weight = weight;
 }

 public String getProviderIp() {
  return providerIp;
 }

 public void setProviderIp(String providerIp) {
  this.providerIp = providerIp;
 }

 public int getProviderPort() {
  return providerPort;
 }

 public void setProviderPort(int providerPort) {
  this.providerPort = providerPort;
 }

 public String getServicekey() {
  return servicekey;
 }

 public void setServicekey(String servicekey) {
  this.servicekey = servicekey;
 }

 public String getServiceGroupName() {
  return serviceGroupName;
 }

 public void setServiceGroupName(String serviceGroupName) {
  this.serviceGroupName = serviceGroupName;
 }

 public long getTimeOut() {
  return timeOut;
 }

 public void setTimeOut(long timeOut) {
  this.timeOut = timeOut;
 }

 private String providerIp;

  private int providerPort;

  //base on netty, and its' netty path for the provider
  private String  servicekey;

  //base on group for service
  private String serviceGroupName;

 public int getWorkerThreads() {
  return workerThreads;
 }

 public void setWorkerThreads(int workerThreads) {
  this.workerThreads = workerThreads;
 }

 private long timeOut;

  private int workerThreads;



}
