package com.kevin.rpc.common;

import lombok.Data;

import java.util.concurrent.Semaphore;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common
 * @Project: Kevin-RPC
 * @Date: 2024/6/25
 * @Description: 服务端限流包装类
 **/
@Data
public class ServerServiceSemaphoreWrapper {

    private Semaphore semaphore;

    private int maxNums;

    public ServerServiceSemaphoreWrapper(int maxNums) {
        this.maxNums = maxNums;
        this.semaphore = new Semaphore(maxNums);
    }
}
