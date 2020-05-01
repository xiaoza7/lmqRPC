package com.lmqrpc.BeanDefinitionRegistrar;

import com.lmqrpc.entity.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 为了实例化服务提供者，并将服务注册到zk
 */


@Component
public class BeanPostProcessorForRpc implements BeanPostProcessor {


    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//       if(beanName.equals("lmqService"))
//       {
//           System.out.println("before initialization!");
//       }

        return bean;
    }


    /**
     * 基于annotation=RpcService.class,来识别serviceprovider,并注册到zk
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        int serverPort();
//        String appKey();
//        long timeout();
//        String groupName()
         Class<?>c=bean.getClass();
        if(c.isAnnotationPresent(RpcService.class)) {
            RpcService myAnno=c.getAnnotation(RpcService.class);
            int serverPort=myAnno.serverPort();
            String appKey=myAnno.appKey();
            long timeout=myAnno.timeout();
            String groupname=myAnno.groupName();



            System.out.println("after initialization! this is test based on annotations------------------------>");



        }

        return bean;
    }


}
