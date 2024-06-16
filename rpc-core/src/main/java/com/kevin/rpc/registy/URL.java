package com.kevin.rpc.registy;

import com.kevin.rpc.registy.zookeeper.ProviderNodeInfo;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class URL {

    /**
     * 服务应用名称 kevin-rpc-client
     */
    private String applicationName;

    /**
     * 注册到节点到服务名称，例如：com.kevin.rpc.interfaces.DataService
     */
    private String serviceName;

    /**
     * 自定义扩展(如：分组、权重、服务提供者的地址、服务提供者的端口 等)
     */
    private Map<String, String> parameters = new HashMap<>();

    public void addParameter(String key, String value) {
        this.parameters.putIfAbsent(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URL url = (URL) o;
        return Objects.equals(applicationName, url.applicationName) && Objects.equals(serviceName, url.serviceName) && Objects.equals(parameters, url.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, serviceName, parameters);
    }

    /**
     * 将URL转换为写入zk的provider节点下的一段字符串
     * kevin-rpc-server;com.kevin.rpc.interfaces.DataService;192.168.9.9:9999;System.currentTimeMillis()).getBytes()
     *
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(URL url) {
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     * kevin-rpc-client;com.kevin.rpc.interfaces.DataService;192.168.9.9;System.currentTimeMillis()).getBytes()
     *
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(URL url) {
        String host = url.getParameters().get("host");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }


    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     * 入参格式例如：/kevin-rpc/com.kevin.interfaces.DataService/provider/192.168.43.227:9092
     *
     * @param providerNodeStr
     * @return
     */
    public static ProviderNodeInfo buildUrlFromUrlStr(String providerNodeStr) {
        String[] items = providerNodeStr.split("/");
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setServiceName(items[2]);
        providerNodeInfo.setAddress(items[4]);
        return providerNodeInfo;
    }

}