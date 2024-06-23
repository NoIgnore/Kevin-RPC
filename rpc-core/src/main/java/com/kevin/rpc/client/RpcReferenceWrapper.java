package com.kevin.rpc.client;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.client
 * @Project: Kevin-RPC
 * @Description: rpc远程调用包装类
 **/
@Data
public class RpcReferenceWrapper<T> {

    private Class<T> aimClass;

    private Map<String, Object> attachments = new ConcurrentHashMap<>();

    public boolean isAsync() {
        return Boolean.parseBoolean(String.valueOf(attachments.get("async")));
    }

    public void setAsync(boolean async) {
        this.attachments.put("async", async);
    }

    public String getUrl() {
        return String.valueOf(attachments.get("url"));
    }

    public void setUrl(String url) {
        attachments.put("url", url);
    }

    public String getServiceToken() {
        return String.valueOf(attachments.get("serviceToken"));
    }

    public void setServiceToken(String serviceToken) {
        attachments.put("serviceToken", serviceToken);
    }

    public String getGroup() {
        return String.valueOf(attachments.get("group"));
    }

    public void setGroup(String group) {
        attachments.put("group", group);
    }
}