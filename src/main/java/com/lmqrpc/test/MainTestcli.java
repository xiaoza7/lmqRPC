package com.lmqrpc.test;


import com.lmqrpc.myservice.AppConfig11;
import com.lmqrpc.myservice.HelloService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainTestcli {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext lmqcontext = new AnnotationConfigApplicationContext(AppConfig11.class);

         HelloService helloService=(HelloService) lmqcontext.getBean("com.lmqrpc.HelloService");
         helloService.getres("this is a test");


    }


}
