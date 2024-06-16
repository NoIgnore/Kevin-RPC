package com.kevin.rpc.common.config;

import lombok.Data;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.config
 * @Project: Kevin-RPC
 **/
@Data
public class ClientConfig {

    private Integer port;

    private String serverAddr;
}