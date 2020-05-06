package com.lmqrpc.BeanDefinitionRegistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

/*
lmq
 */


public class LmqRpcImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanFactoryAware {


    private ResourceLoader resourceLoader;

    private BeanFactory beanFactory;


    private static Logger log=LoggerFactory.getLogger(LmqRpcImportBeanDefinitionRegistrar.class);


    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        /**
         * 得到bd,或者类信息，这里对于接口类，假设实现创建代理对象,
         */
        //这里先写死去为某个接口去创建动态代理对象，其实可以模拟扫描注解的接口，类似mybatis的mapperscan, UserMapper是个接口


        //扫描注解
        Map<String, Object> annotationAttributes = importingClassMetadata
                .getAnnotationAttributes(ComponentScan.class.getName());
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");

        LmqRpcClassPathBeanDefinitionScanner scanner = new LmqRpcClassPathBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(resourceLoader);
        scanner.registerFilters();
        Set<BeanDefinition> beset= scanner.doScanRpc(basePackages); //待选接口除了加@LmqRPC,

       for(BeanDefinition bdh: beset) {
             //sbd.getMetadata().getClassName()
           log.info("when regsiter a new rpc class, the bean name is: "+bdh.getBeanClassName());
           System.out.println("when regsiter a new rpc class, the bean name is: "+bdh.getBeanClassName());
           try {
               String canclassname=bdh.getBeanClassName();
               Class<?> a1 = Class.forName(canclassname);

               Class a2[] = a1.getInterfaces();
               if(a2.length>0)
               {
                   //父类接口很重要，为了和服务提供者的类名一致
                   canclassname=a2[0].getName();
               }

               // BeanDefinitionBuilder beanDefinitionBuilder= BeanDefinitionBuilder.genericBeanDefinition(userMapper.getClass()); //实际无法获取代理类信息，使用factorybean
               BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(canclassname);
               GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionBuilder.getBeanDefinition();

               //为了传参数，可以给bd添加构造函数
               beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(canclassname);
               beanDefinition.setBeanClass(MyLmqRpcFactoryBean.class); //MyFactoryBean不能加Componenrt注解
               registry.registerBeanDefinition(canclassname, beanDefinition);
           }catch (Exception e)
           {
               e.printStackTrace();
           }


       }
    }


    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
          this.beanFactory=beanFactory;
    }


    public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader=resourceLoader;
    }
}
