package com.lmqrpc.myservice;


import com.lmqrpc.entity.RpcService;
import org.springframework.stereotype.Component;

@Component
@RpcService(serverPort = 7000,appKey ="testhaha",timeout = 6000,groupName = "cons")
public class HelloServiceImpl implements HelloService {

  //  appKey = "testhaha",groupName = "cons"
    @Override
    public String getres(String hh) {
        return "welcome to lmqrpc; the res is -->"+hh;
    }
}
