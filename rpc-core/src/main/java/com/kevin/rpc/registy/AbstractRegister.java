package com.kevin.rpc.registy;

import java.util.List;

import static com.kevin.rpc.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static com.kevin.rpc.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.registy
 * @Project: Kevin-RPC
 **/
public abstract class AbstractRegister implements RegistryService {


    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url.getServiceName());
    }

    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

    /**
     * 留给子类扩展
     * 订阅操作执行之前需要执行的逻辑
     *
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);

    /**
     * 留给子类扩展
     * 订阅操作执行之后需要执行的逻辑
     *
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类扩展
     * 获取服务提供者的ip
     * List<String> of /kevin-rpc/com.kevin.rpc.interfaces.DataService/provider/ all available 192.?.?.? : ????
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

}