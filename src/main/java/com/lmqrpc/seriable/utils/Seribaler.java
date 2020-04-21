package com.lmqrpc.seriable.utils;

public interface Seribaler {


    public <T> byte[] serialize(T object);

    public <T> T deserialize(byte buffer[], Class<T> classname);


}
