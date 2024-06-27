package com.kevin.rpc.provider.springboot.service.impl;

import com.kevin.rpc.interfaces.UserService;
import com.kevin.rpc.spring.starter.common.EasyRpcService;

@EasyRpcService
public class UserServiceImpl implements UserService {

    @Override
    public void test() {
        System.out.println("UserServiceImpl : test");
    }
}