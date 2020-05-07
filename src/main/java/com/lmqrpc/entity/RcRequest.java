package com.lmqrpc.entity;

import java.io.Serializable;

public class RcRequest implements Serializable {


    private String uniqueId;

    private ReServiceProvider provider;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public ReServiceProvider getProvider() {
        return provider;
    }

    public void setProvider(ReServiceProvider provider) {
        this.provider = provider;
    }

    public String getTargetMethodName() {
        return targetMethodName;
    }

    public void setTargetMethodName(String targetMethodName) {
        this.targetMethodName = targetMethodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getWebappName() {
        return webappName;
    }

    public void setWebappName(String webappName) {
        this.webappName = webappName;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private String targetMethodName;

    private Object[]args;

    private String webappName;

    //
    private long timeout;


}
