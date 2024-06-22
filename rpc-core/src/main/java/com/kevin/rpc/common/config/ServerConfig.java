package com.kevin.rpc.common.config;

import lombok.Data;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.config
 * @Project: Kevin-RPC
 **/
@Data
public class ServerConfig {

    private Integer port;

    private String registerAddr;

    private String applicationName;

    /**
     * 服务端序列化方式 example: hessian2,kryo,jdk,fastjson
     */
    private String serverSerialize;
}
