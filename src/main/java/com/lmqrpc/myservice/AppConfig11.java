package com.lmqrpc.myservice;



import com.lmqrpc.BeanDefinitionRegistrar.LmqRpcImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration

@ComponentScan({"com.lmqrpc.BeanDefinitionRegistrar","com.lmqrpc.myservice"})
@Import( LmqRpcImportBeanDefinitionRegistrar.class)
public class AppConfig11 {



}







