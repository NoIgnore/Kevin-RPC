package com.kevin.rpc.server;

import com.kevin.rpc.common.event.RpcDestroyEvent;
import com.kevin.rpc.common.event.RpcListenerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerShutdownHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerShutdownHook.class);

    /**
     * 注册一个shutdownHook的钩子，当jvm进程关闭的时候触发
     */
    public static void registryShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                RpcListenerLoader.sendSyncEvent(new RpcDestroyEvent("destroy"));
                LOGGER.info("server destruction");
            }
        }));
    }

}