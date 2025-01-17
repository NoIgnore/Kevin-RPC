package com.kevin.rpc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.listener
 * @Project: Kevin-RPC
 **/
@Getter
@AllArgsConstructor
public class RpcUpdateEvent implements RpcEvent {

    @Override
    public RpcEvent setData(Object data) {
        this.data = data;
        return this;
    }

    private Object data;
}