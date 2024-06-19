package com.kevin.rpc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RpcDestroyEvent implements RpcEvent {

    private Object data;

    @Override
    public RpcDestroyEvent setData(Object data) {
        this.data = data;
        return this;
    }
}