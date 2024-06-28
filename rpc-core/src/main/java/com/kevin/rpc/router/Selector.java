package com.kevin.rpc.router;

import lombok.Data;

@Data
public class Selector {

    /**
     * 服务命名
     * eg: com.xxx.xxx.DataService
     */
    private String providerServiceName;

}