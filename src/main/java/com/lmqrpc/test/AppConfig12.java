package com.lmqrpc.test;



import com.lmqrpc.beanDefinitionRegistrar.LmqRpcImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@ComponentScan("com.lmqrpc.myservice")
@Import( LmqRpcImportBeanDefinitionRegistrar.class)
public class AppConfig12 {



}







