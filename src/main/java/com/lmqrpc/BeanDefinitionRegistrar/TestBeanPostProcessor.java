package com.lmqrpc.BeanDefinitionRegistrar;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 这里给provideserivce的类加@RpcService注解，然后beanPostProcessor实例化后，依据bean的class注解进行服务注册，注意注册的异常过程分析，return 一定要完成
 */

@Component
public class TestBeanPostProcessor implements BeanPostProcessor {


    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
       if(beanName.equals("lmqService"))
       {
           System.out.println("before initialization!");
       }

        return bean;
    }


    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(beanName.equals("lmqService"))
        {
            System.out.println("after initialization!");
        }

        return bean;
    }



}
