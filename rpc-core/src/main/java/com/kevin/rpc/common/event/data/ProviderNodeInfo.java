package com.kevin.rpc.common.event.data;

import lombok.Data;

@Data
public class ProviderNodeInfo {

    private String serviceName;

    private String address;

    private Integer weight;

    @Override
    public String toString() {
        return "ProviderNodeInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
