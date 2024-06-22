package com.kevin.rpc.serialize;

/**
 * @Author HHJ
 * @Package com.kevin.rpc.serialize
 * @Project Kevin-RPC
 * @Date 2024/6/19
 **/
public interface SerializeFactory {


    /**
     * 序列化
     *
     * @param t
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
