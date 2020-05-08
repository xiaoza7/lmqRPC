package com.lmqrpc.test;


import com.lmqrpc.myservice.HelloService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainTestcli {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext lmqcontext = new AnnotationConfigApplicationContext(AppConfig12.class);

         HelloService helloService=(HelloService) lmqcontext.getBean("com.lmqrpc.myservice.HelloService");
         for(int i=0;i<20;i++) {
             String ss = helloService.getres("this is a test");
             System.out.println("-----------------------*****************   " + ss);
         }


    }


}
