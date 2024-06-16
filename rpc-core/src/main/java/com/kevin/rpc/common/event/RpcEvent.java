package com.kevin.rpc.common.event;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event
 * @Project: Kevin-RPC
 **/
public interface RpcEvent {

    /**
     * 获取事件数据
     *
     * @return
     */
    Object getData();

    /**
     * 设置数据
     *
     * @param data
     * @return
     */
    RpcEvent setData(Object data);
}