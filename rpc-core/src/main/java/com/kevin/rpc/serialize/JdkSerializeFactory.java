package com.kevin.rpc.serialize;

import java.io.*;

public class JdkSerializeFactory implements SerializeFactory {


    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.writeObject(t);
            output.flush();
            output.close();
            data = os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream input = new ObjectInputStream(is);
            Object result = input.readObject();
            //return ((T) result);
            return clazz.cast(result); // 使用 clazz.cast 进行类型转换
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}