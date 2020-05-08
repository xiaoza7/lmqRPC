package com.lmqrpc.test;



import com.lmqrpc.BeanDefinitionRegistrar.LmqRpcImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@ComponentScan({"com.lmqrpc.BeanDefinitionRegistrar","com.lmqrpc.myserviceImpl"})
public class AppConfig11 {



}







