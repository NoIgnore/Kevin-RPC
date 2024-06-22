package com.kevin.rpc.common.config;

import lombok.Data;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.common.config
 * @Project: Kevin-RPC
 **/
@Data
public class ClientConfig {

    private String registerAddr;

    private String applicationName;

    /**
     * 客户端序列化方式 example: hessian2,kryo,jdk,fastjson
     */
    private String clientSerialize;

    /**
     * 代理类型 example: jdk,javassist
     */
    private String proxyType;

    /**
     * 负载均衡策略 example:random,rotate
     */
    private String routerStrategy;
}
