package com.lmqrpc.utils;

import java.io.Serializable;

public class RpcJsonFormat<T>  implements Serializable {

    private boolean resultSuccess;



    private String resultCode;

    private T data;

    public static <T> RpcJsonFormat<T> buildJsonFromdata( T data)
    {
        RpcJsonFormat<T> rpcJsonFormat=new RpcJsonFormat<T>();

        rpcJsonFormat.setData(data);
        rpcJsonFormat.setResultSuccess(true);
        rpcJsonFormat.setResultCode("200");

        return rpcJsonFormat;
    }

    public boolean isResultSuccess() {
        return resultSuccess;
    }

    public void setResultSuccess(boolean resultSuccess) {
        this.resultSuccess = resultSuccess;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
