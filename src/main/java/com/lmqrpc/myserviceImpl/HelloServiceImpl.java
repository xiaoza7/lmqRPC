package com.lmqrpc.myserviceImpl;


import com.lmqrpc.entity.RpcService;
import com.lmqrpc.myservice.HelloService;
import org.springframework.stereotype.Component;

@Component
@RpcService(serverPort = 7000,appKey ="testhaha",timeout = 6000,groupName = "cons")
public class HelloServiceImpl implements HelloService {

  //  appKey = "testhaha",groupName = "cons"
    @Override
    public String getres(String hh) {

        System.out.println("begin call from rpc cli-------------------> the parameter is: "+hh);
        return "welcome to lmqrpc; the res is -->"+hh;
    }
}
