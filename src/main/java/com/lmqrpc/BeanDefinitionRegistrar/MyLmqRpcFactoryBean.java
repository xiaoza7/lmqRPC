package com.lmqrpc.BeanDefinitionRegistrar;


import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class MyLmqRpcFactoryBean implements FactoryBean {
    Class clazz;
    public MyLmqRpcFactoryBean(Class clazz)
    {
        this.clazz=clazz;
    }


    public Object getObject() throws Exception {

        Class []clazz= new Class[]{this.clazz};
               Object  proxy=  Proxy.newProxyInstance(getClass().getClassLoader(),clazz, new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                System.out.println("in proxy");

                //为了模仿mybatis执行sql
//                Method method1 =proxy.getClass().getInterfaces()[0].getMethod(method.getName(),String.class);
//                Select select =method1.getDeclaredAnnotation(Select.class);
//                System.out.println(select.value()[0]);

                return null;
            }
        });
    return proxy;
    }


    public Class<?> getObjectType() {
        return null;
    }


    public boolean isSingleton() {
        return false;
    }
}
