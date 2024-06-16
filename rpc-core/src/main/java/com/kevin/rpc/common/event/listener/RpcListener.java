package com.kevin.rpc.common.event.listener;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.listener
 * @Project: Kevin-RPC
 **/
public interface RpcListener<T> {

    /**
     * 事件回调方法
     *
     * @param o
     */
    void callBack(Object o);

}