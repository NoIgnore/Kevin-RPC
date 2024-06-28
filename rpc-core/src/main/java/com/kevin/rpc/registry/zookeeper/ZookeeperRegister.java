package com.kevin.rpc.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kevin.rpc.common.event.RpcEvent;
import com.kevin.rpc.common.event.RpcListenerLoader;
import com.kevin.rpc.common.event.RpcNodeUpdateEvent;
import com.kevin.rpc.common.event.RpcUpdateEvent;
import com.kevin.rpc.common.event.data.ProviderNodeInfo;
import com.kevin.rpc.common.event.data.URLChangeWrapper;
import com.kevin.rpc.common.utils.CommonUtil;
import com.kevin.rpc.registry.AbstractRegister;
import com.kevin.rpc.registry.RegistryService;
import com.kevin.rpc.registry.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kevin.rpc.common.cache.CommonClientCache.CLIENT_CONFIG;
import static com.kevin.rpc.common.cache.CommonServerCache.SERVER_CONFIG;

public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegister.class);

    private final AbstractZookeeperClient zkClient;

    private final String ROOT = "/kevin-rpc";

    public ZookeeperRegister() {
        String registryAddr = CLIENT_CONFIG != null ? CLIENT_CONFIG.getRegisterAddr() : SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
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
     * List<String> of [/kevin-rpc/com.kevin.rpc.interfaces.DataService/provider/]’s all available -> (ip : port)
     *
     * @param serviceName
     * @return
     */
    @Override
    public List<String> getProviderIps(String serviceName) {
        return this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
    }

    /**
     * @param serviceName
     * @return Map<String1, String2>
     * @Description: return com.kevin.rpc.interfaces.DataService
     * -> <<ip:host,urlString>,<ip:host,urlString>,<ip:host,urlString>>
     */
    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>(16);
        for (String ipAndHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipAndHost);
            result.put(ipAndHost, childData);
        }
        return result;
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
        //监听节点内部的数据变化
        String providerIpStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, new TypeReference<List<String>>() {
        });
        for (String providerIp : providerIpList) {
            this.watchNodeDataChange(newServerNodePath + "/" + providerIp);
        }
    }

    public void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, watchedEvent -> {
            // /kevin-rpc/com.kevin.rpc.interfaces.DataService/provider
            String path = watchedEvent.getPath();
            LOGGER.info("[watchChildNodeData] 监听到zk节点下的{}节点数据发生变更", path);
            List<String> childrenDataList = zkClient.getChildrenData(path);
            //List<String> childrenDataList -> “服务端IP:服务端端口” 的List
            URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
            Map<String, String> nodeDetailInfoMap = new HashMap<>();
            for (String providerAddress : childrenDataList) {
                String nodeDetailInfo = zkClient.getNodeData(path + "/" + providerAddress);
                // nodeDetailInfo-> 应用名;com.kevin.rpc.interfaces.DataService;服务端IP:服务端端口;当前时间;权重;group
                nodeDetailInfoMap.put(providerAddress, nodeDetailInfo);
            }
            urlChangeWrapper.setNodeDataUrl(nodeDetailInfoMap);
            urlChangeWrapper.setProviderUrl(childrenDataList);
            // setServiceName：com.kevin.rpc.interfaces.DataService
            urlChangeWrapper.setServiceName(path.split("/")[2]);
            RpcEvent rpcEvent = new RpcUpdateEvent(urlChangeWrapper);
            RpcListenerLoader.sendEvent(rpcEvent);
            //收到回调之后在注册一次监听，这样能保证一直都收到消息
            watchChildNodeData(path);
        });
    }

    /**
     * 订阅服务节点内部的数据变化（节点对应的内部数据的变化）
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, watchedEvent -> {
            String path = watchedEvent.getPath();
            LOGGER.info("[watchNodeDataChange]收到子节点{}数据变化", path);
            String nodeData = zkClient.getNodeData(path);
            //nodeData -> 应用名;com.kevin.rpc.interfaces.服务名;服务端IP:服务端端口;当前时间;权重;group
            if (CommonUtil.isEmpty(nodeData)) {
                LOGGER.error("{} node data is null", path);
            } else {
                ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(nodeData);
                RpcEvent rpcEvent = new RpcNodeUpdateEvent(providerNodeInfo);
                RpcListenerLoader.sendEvent(rpcEvent);
            }
            watchNodeDataChange(newServerNodePath);
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
