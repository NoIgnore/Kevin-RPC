package com.kevin.rpc.serialize;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class HessianSerializeFactory implements SerializeFactory {

    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(t);
            output.getBytesOutputStream().flush();
            output.completeMessage();
            output.close();
            data = os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        Object result = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            Hessian2Input input = new Hessian2Input(is);
            result = input.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return (T) result; // 强制类型转换
        return clazz.cast(result); // 使用 clazz.cast 进行类型转换
    }

}