package com.kevin.rpc.registy;

import com.kevin.rpc.common.event.data.ProviderNodeInfo;
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
     * kevin-rpc-server;com.kevin.rpc.interfaces.DataService;服务端IP:服务端端口;当前时间;100;group
     * 这里的100是权重值
     *
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(URL url) {
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        String group = url.getParameters().get("group");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis() + ";100;" + group).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     * kevin-rpc-client;com.kevin.rpc.interfaces.DataService;客户端IP;当前时间
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
     * 实际入参: kevin-rpc-server;com.kevin.rpc.interfaces.DataService;服务端IP:服务端端口;当前时间;100(权重);default
     *
     * @param providerNodeStr
     * @return
     */
    public static ProviderNodeInfo buildUrlFromUrlStr(String providerNodeStr) {
        String[] items = providerNodeStr.split(";");
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setApplicationName(items[0]);
        providerNodeInfo.setServiceName(items[1]);
        providerNodeInfo.setAddress(items[2]);
        providerNodeInfo.setWeight(Integer.valueOf(items[4]));
        providerNodeInfo.setGroup(String.valueOf(items[5]));
        return providerNodeInfo;
    }

}