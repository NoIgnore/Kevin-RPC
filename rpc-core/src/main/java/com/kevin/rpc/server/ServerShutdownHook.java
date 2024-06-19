package com.kevin.rpc.server;

import com.kevin.rpc.common.event.RpcDestroyEvent;
import com.kevin.rpc.common.event.RpcListenerLoader;

public class ServerShutdownHook {

    /**
     * 注册一个shutdownHook的钩子，当jvm进程关闭的时候触发
     */
    public static void registryShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                RpcListenerLoader.sendSyncEvent(new RpcDestroyEvent("destroy"));
                System.out.println("server destruction");
            }
        }));
    }

}