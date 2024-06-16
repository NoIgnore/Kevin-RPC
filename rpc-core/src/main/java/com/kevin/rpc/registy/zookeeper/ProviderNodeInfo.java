package com.kevin.rpc.registy.zookeeper;

import lombok.Data;

@Data
public class ProviderNodeInfo {

    private String serviceName;

    private String address;

    @Override
    public String toString() {
        return "ProviderNodeInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
