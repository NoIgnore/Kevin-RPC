package com.kevin.rpc.router;

import lombok.Data;

@Data
public class Selector {

    /**
     * 服务命名
     * eg: com.shaogezhu.test.DataService
     */
    private String providerServiceName;

}