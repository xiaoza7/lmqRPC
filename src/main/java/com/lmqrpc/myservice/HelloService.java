package com.lmqrpc.myservice;

import com.lmqrpc.entity.LmqRPC;
import org.springframework.stereotype.Component;


@LmqRPC(appKey = "testhaha",groupName = "cons")
public interface HelloService {

    public String getres(String hh);
}
