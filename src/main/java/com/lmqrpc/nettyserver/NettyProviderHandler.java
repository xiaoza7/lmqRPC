package com.lmqrpc.nettyserver;


import com.lmqrpc.entity.RcRequest;
import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.invoker.InvokerServiceFactory;
import com.lmqrpc.register.RegisterHandler;
import com.lmqrpc.register.RegisterHandlerZk;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

@ChannelHandler.Sharable
public class NettyProviderHandler  extends SimpleChannelInboundHandler<RcRequest> {

    private static final Logger log=LoggerFactory.getLogger(NettyProviderHandler.class);

    protected void channelRead0(ChannelHandlerContext ctx, RcRequest rcRequest) throws Exception {

        if(ctx.channel().isWritable())
        {
            if(rcRequest==null)
            {
                log.info(" when handler process, the resquest is null.........");
                ctx.close();
            }
            ReServiceProvider reServiceProvider=rcRequest.getProvider();
            Object result = null;
            //serviceprovider class name
            String serviceClasskey=reServiceProvider.getTargetClass().getName();

            String methodName=rcRequest.getTargetMethodName();
            int timeout=rcRequest.getTimeout();
            RegisterHandler registerHandlerZk=RegisterHandlerZk.singleton();

            // for providers, get its' local providers
            List<ReServiceProvider> serviceProviderList=((RegisterHandlerZk) registerHandlerZk).getServiceProviderListByServiceKeyFromZk(serviceClasskey);

            //look for the provider  based on methodName
            ReServiceProvider candidateProvider=null;

            for(ReServiceProvider reServiceProvider1: serviceProviderList)
            {
                if(reServiceProvider1.getTargetMethod().getName().equals(methodName))
                {
                    candidateProvider=reServiceProvider1;
                    break;
                }
            }

            if(candidateProvider==null){
                log.info(" can not find the provider based on the method name");
            }

            Method method=candidateProvider.getTargetMethod();

           result=method.invoke(candidateProvider,rcRequest.getArgs());









        }

    }
}
