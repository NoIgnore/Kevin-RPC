package com.kevin.rpc.spring.starter.config;

import com.kevin.rpc.client.Client;
import com.kevin.rpc.client.RpcReference;
import com.kevin.rpc.client.RpcReferenceWrapper;
import com.kevin.rpc.spring.starter.common.KevinRpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;

import static com.kevin.rpc.common.cache.CommonClientCache.CLIENT_CONFIG;


/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.spring.starter.config
 * @Project: Kevin-RPC
 * @Date: 2024/6/25
 * @Description: 客户端自动配置类
 **/
public class RpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {

    private static RpcReference rpcReference = null;
    private static Client client = null;
    private volatile boolean needInitClient = false;
    private volatile boolean hasInitClientConfig = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientAutoConfiguration.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 这里的bean是com.kevin.rpc.consumer.springboot.controller的UserController
        // beanName 通常是类名的首字母小写形式，例如 "userController"
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            //@KevinRpcReference
            //private UserService userService;
            //
            ///**
            // * 验证各类参数配置是否异常
            // */
            //@KevinRpcReference(group = "data-group", serviceToken = "data-token")
            //private DataService dataService;
            //这里的field有userService和dataService

            if (field.isAnnotationPresent(KevinRpcReference.class)) {
                if (!hasInitClientConfig) {
                    client = new Client();
                    try {
                        client.initClientConfig();
                        rpcReference = client.initClientApplication();
                    } catch (Exception e) {
                        LOGGER.error("[IRpcClientAutoConfiguration] postProcessAfterInitialization has error ", e);
                        throw new RuntimeException(e);
                    }
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                KevinRpcReference kevinRpcReference = field.getAnnotation(KevinRpcReference.class);
                try {
                    field.setAccessible(true);
                    Object refObj = field.get(bean);
                    RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(kevinRpcReference.group());
                    rpcReferenceWrapper.setServiceToken(kevinRpcReference.serviceToken());
                    rpcReferenceWrapper.setUrl(kevinRpcReference.url());
                    rpcReferenceWrapper.setTimeOut(kevinRpcReference.timeOut());
                    //失败重试次数
                    rpcReferenceWrapper.setRetry(kevinRpcReference.retry());
                    rpcReferenceWrapper.setAsync(kevinRpcReference.async());
                    refObj = rpcReference.get(rpcReferenceWrapper);
                    field.set(bean, refObj);
                    //使用 field.set(bean, refObj) 将代理对象注入到 UserController 实例的对应字段中
                    //这里的field是UserService或DataService
                    client.doSubscribeService(field.getType());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (needInitClient && client != null) {
            LOGGER.info(" ================== [{}] started success ================== ", CLIENT_CONFIG.getApplicationName());
            client.doConnectServer();
            client.startClient();
        }
    }
}