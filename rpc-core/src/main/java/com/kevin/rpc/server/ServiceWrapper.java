package com.kevin.rpc.server;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.server
 * @Project: Kevin-RPC
 * @Date: 2024/6/23
 * @Description: ServiceWrapper
 **/
@Data
@RequiredArgsConstructor
public class ServiceWrapper {
    /**
     * 对外暴露的具体服务对象
     */
    @NotNull
    private final Object serviceBean;

    /**
     * 具体暴露服务的分组
     */
    private String group = "default";

    /**
     * 整个应用的token校验
     */
    private String serviceToken = "";

    /**
     * 限流策略
     */
    private Integer limit = -1;
}
