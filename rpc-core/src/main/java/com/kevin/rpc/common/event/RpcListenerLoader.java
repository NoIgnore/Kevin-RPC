package com.kevin.rpc.common.event;

import com.kevin.rpc.common.event.listener.ProviderNodeUpdateListener;
import com.kevin.rpc.common.event.listener.RpcListener;
import com.kevin.rpc.common.event.listener.ServiceDestroyListener;
import com.kevin.rpc.common.event.listener.ServiceUpdateListener;
import com.kevin.rpc.common.utils.CommonUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.event.listener
 * @Project: Kevin-RPC
 **/
public class RpcListenerLoader {

    private static final List<RpcListener<?>> rpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public void init() {
        rpcListenerList.add(new ServiceUpdateListener());
        rpcListenerList.add(new ServiceDestroyListener());
        rpcListenerList.add(new ProviderNodeUpdateListener());
    }

    public static void sendEvent(RpcEvent rpcEvent) {
        if (CommonUtil.isEmptyList(rpcListenerList)) {
            return;
        }
        for (RpcListener<?> rpcListener : rpcListenerList) {
            Class<?> type = getInterfaceT(rpcListener);
            if (type != null && type.equals(rpcEvent.getClass())) {
                eventThreadPool.execute(() -> {
                    try {
                        rpcListener.callBack(rpcEvent.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * 同步事件处理，可能会堵塞, 暂定传入的是一个 RpcDestroyEvent
     */
    public static void sendSyncEvent(RpcEvent iRpcEvent) {
        System.out.println("rpcListenerList：" + rpcListenerList);
        if (CommonUtil.isEmptyList(rpcListenerList)) {
            return;
        }
        /**
         * rpcListenerList.add(new ServiceUpdateListener());
         * rpcListenerList.add(new ServiceDestroyListener()); ServiceDestroyListener implements RpcListener<RpcDestroyEvent>
         * rpcListenerList.add(new ProviderNodeUpdateListener());
         */
        for (RpcListener<?> rpcListener : rpcListenerList) {
            Class<?> type = getInterfaceT(rpcListener);
            // if( RpcDestroyEvent.class == RpcDestroyEvent.class)
            if (type != null && type.equals(iRpcEvent.getClass())) {
                try {
                    // ServiceDestroyListener.callBack(RpcDestroyEvent.getData)
                    rpcListener.callBack(iRpcEvent.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取接口上的泛型T
     */
    public static Class<?> getInterfaceT(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }
}