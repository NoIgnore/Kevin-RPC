package com.kevin.rpc.registy.zookeeper;

import com.kevin.rpc.common.event.RpcEvent;
import com.kevin.rpc.common.event.RpcListenerLoader;
import com.kevin.rpc.common.event.RpcUpdateEvent;
import com.kevin.rpc.common.event.data.URLChangeWrapper;
import com.kevin.rpc.registy.AbstractRegister;
import com.kevin.rpc.registy.RegistryService;
import com.kevin.rpc.registy.URL;

import java.util.List;

public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private final AbstractZookeeperClient zkClient;

    private final String ROOT = "/kevin-rpc";

    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    /**
     * /kevin-rpc/com.kevin.rpc.interfaces.DataService/provider/192.???.???.???:7890
     *
     * @param url
     * @return
     */
    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    /**
     * /kevin-rpc/com.kevin.rpc.interfaces.DataService/consumer/kevin-rpc-client:192.???.???.???:
     *
     * @param url
     * @return
     */
    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host") + ":";
    }

    /**
     * List<String> of /kevin-rpc/com.kevin.rpc.interfaces.DataService/provider/ all available 192.?.?.? : ????
     *
     * @param serviceName
     * @return
     */
    @Override
    public List<String> getProviderIps(String serviceName) {
        return this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
    }


    @Override
    public void register(URL url) {
        if (!zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildProviderUrlStr(url);
        if (zkClient.existNode(getProviderPath(url))) {
            zkClient.deleteNode(getProviderPath(url));
        }
        zkClient.createTemporaryData(getProviderPath(url), urlStr);
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (zkClient.existNode(getConsumerPath(url))) {
            zkClient.deleteNode(getConsumerPath(url));
        }
        zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        super.subscribe(url);
    }

    /**
     * 监听是否有新的服务注册进 /kevin-rpc/com.kevin.rpc.interfaces.DataService/provider
     *
     * @param url
     */
    @Override
    public void doAfterSubscribe(URL url) {
        //监听是否有新的服务注册
        String newServerNodePath = ROOT + "/" + url.getServiceName() + "/provider";
        watchChildNodeData(newServerNodePath);
    }

    public void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, watchedEvent -> {
            System.out.println(watchedEvent);
            // /kevin-rpc/com.kevin.rpc.interfaces.DataService/provider
            String path = watchedEvent.getPath();
            List<String> childrenDataList = zkClient.getChildrenData(path);
            URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
            urlChangeWrapper.setProviderUrl(childrenDataList);
            // setServiceName：com.kevin.rpc.interfaces.DataService
            urlChangeWrapper.setServiceName(path.split("/")[2]);
            RpcEvent rpcEvent = new RpcUpdateEvent(urlChangeWrapper);
            RpcListenerLoader.sendEvent(rpcEvent);
            //收到回调之后在注册一次监听，这样能保证一直都收到消息
            watchChildNodeData(path);
        });
    }

    @Override
    public void doBeforeSubscribe(URL url) {

    }

    @Override
    public void doUnSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }

}
