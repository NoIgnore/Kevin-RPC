package com.kevin.rpc.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.listener
 * @Project: Kevin-RPC
 **/
@Data
@AllArgsConstructor
public class RpcUpdateEvent implements RpcEvent {
    private Object data;
}