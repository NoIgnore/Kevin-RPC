package com.kevin.rpc.common.event.data;

import lombok.Data;

import java.util.List;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.data
 * @Project: Kevin-RPC
 **/
@Data
public class URLChangeWrapper {

    private String serviceName;

    private List<String> providerUrl;

    @Override
    public String toString() {
        return "URLChangeWrapper{" +
                "serviceName='" + serviceName + '\'' +
                ", providerUrl=" + providerUrl +
                '}';
    }
}