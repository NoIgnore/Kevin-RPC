package com.kevin.rpc.common.event;

import com.kevin.rpc.common.event.listener.RpcListener;
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

    private static List<RpcListener<?>> rpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public static void registerListener(RpcListener<?> rpcListener) {
        rpcListenerList.add(rpcListener);
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
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