package com.kevin.rpc.consumer.springboot.controller;

import com.kevin.rpc.interfaces.DataService;
import com.kevin.rpc.interfaces.UserService;
import com.kevin.rpc.spring.starter.common.EasyRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.consumer.springboot.controller
 * @Project: Kevin-RPC
 * @Date: 2024/6/26
 * @Description: Null
 **/
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @EasyRpcReference
    private UserService userService;

    /**
     * 验证各类参数配置是否异常
     */
    @EasyRpcReference(group = "data-group", serviceToken = "data-token")
    private DataService dataService;

    @GetMapping(value = "/test")
    public void test() {
        userService.test();
    }


    @GetMapping(value = "/send/{msg}")
    public String testMaxData(@PathVariable(name = "msg") String msg) {
        return dataService.sendData(msg);
    }


    @GetMapping(value = "/list")
    public List<String> getOrderNo() {
        return dataService.getList();
    }

}
