package com.lmqrpc.seriable.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaDeaultseirabler implements  Seribaler{


    public <T> byte[] serialize(T object) {

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();

        try{
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();

        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {

        }

        return byteArrayOutputStream.toByteArray();
    }

    public <T> T deserialize(byte[] buffer, Class<T> classname) {

        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(buffer);
        try{
            ObjectInputStream objectInputStream=new ObjectInputStream(byteArrayInputStream);

            return (T) objectInputStream.readObject();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

      return null;
    }

}
