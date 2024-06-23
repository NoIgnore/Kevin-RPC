package com.kevin.rpc.common.event.data;

import lombok.Data;

@Data
public class ProviderNodeInfo {

    private String applicationName;

    private String serviceName;

    private String address;

    private Integer weight;

    private String group;

    @Override
    public String toString() {
        return "ProviderNodeInfo{" +
                "applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                ", weight=" + weight +
                ", group='" + group + '\'' +
                '}';
    }
}
