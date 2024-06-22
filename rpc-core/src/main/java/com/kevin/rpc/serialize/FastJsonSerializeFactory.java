package com.kevin.rpc.serialize;

import com.alibaba.fastjson.JSON;

/**
 * @Author HHJ
 * @Package com.kevin.rpc.serialize
 * @Project Kevin-RPC
 * @Date 2024/6/19
 **/
public class FastJsonSerializeFactory implements SerializeFactory {

    @Override
    public <T> byte[] serialize(T t) {
        String jsonStr = JSON.toJSONString(t);
        return jsonStr.getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(new String(data), clazz);
    }

}
