package com.kevin.rpc.provider.springboot.service.impl;

import com.kevin.rpc.interfaces.DataService;
import com.kevin.rpc.spring.starter.common.KevinRpcService;

import java.util.ArrayList;
import java.util.List;

@KevinRpcService(serviceToken = "data-token", group = "data-group", limit = 2)
public class DataServiceImpl implements DataService {

    @Override
    public String sendData(String body) {
        System.out.println("这里是服务提供者，body is " + body);
        return "success from server";
    }

    @Override
    public List<String> getList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("rpc.provider.springboot.service.impl 实现的 DataServiceImpl:getList() : List[0]");
        arrayList.add("rpc.provider.springboot.service.impl 实现的 DataServiceImpl:getList() : List[1]");
        arrayList.add("rpc.provider.springboot.service.impl 实现的 DataServiceImpl:getList() : List[2]");
        return arrayList;
    }

    @Override
    public void testError() {
        System.out.println(1 / 0);
    }

    @Override
    public String testErrorV2() {
        throw new RuntimeException("测试异常");
    }

}