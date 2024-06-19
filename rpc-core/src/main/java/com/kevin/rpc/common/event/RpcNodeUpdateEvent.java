package com.kevin.rpc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RpcNodeUpdateEvent implements RpcEvent {

    private Object data;

    @Override
    public RpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}