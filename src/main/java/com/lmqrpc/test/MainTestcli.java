package com.lmqrpc.test;


import com.lmqrpc.myservice.AppConfig12;
import com.lmqrpc.myservice.HelloService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainTestcli {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext lmqcontext = new AnnotationConfigApplicationContext(AppConfig12.class);

         HelloService helloService=(HelloService) lmqcontext.getBean("com.lmqrpc.myservice.HelloService");
        String ss= helloService.getres("this is a test");
        System.out.println("-----------------------*****************   "+ss);


    }


}
