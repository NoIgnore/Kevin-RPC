package com.kevin.rpc.server.impl;

import com.kevin.rpc.interfaces.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public void test() {
        System.out.println("UserServiceImpl : test");
    }
}