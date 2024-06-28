package com.kevin.rpc.spring.starter.config;

import com.kevin.rpc.server.Server;
import com.kevin.rpc.server.ServerShutdownHook;
import com.kevin.rpc.server.ServiceWrapper;
import com.kevin.rpc.spring.starter.common.KevinRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static com.kevin.rpc.common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * @Author: HHJ
 * @Package: com.kevin.rpc.spring.starter.config
 * @Project: Kevin-RPC
 * @Date: 2024/6/26
 * @Description: 服务端自动配置类
 **/
public class RpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(KevinRpcService.class);
        if (beanMap.size() == 0) {
            //说明当前应用内部不需要对外暴露服务
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        server = new Server();
        server.initServerConfig();
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            KevinRpcService kevinRpcService = bean.getClass().getAnnotation(KevinRpcService.class);
            ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(bean, kevinRpcService.group());
            dataServiceServiceWrapper.setServiceToken(kevinRpcService.serviceToken());
            dataServiceServiceWrapper.setWeight(kevinRpcService.weight());
            dataServiceServiceWrapper.setLimit(kevinRpcService.limit());
            server.registryService(dataServiceServiceWrapper);
            LOGGER.info(">>>>>>>>>>>>>>> [easy-rpc] {} export success! >>>>>>>>>>>>>>> ", beanName);
        }
        ServerShutdownHook.registryShutdownHook();
        server.startServerApplication();
        long end = System.currentTimeMillis();
        LOGGER.info(" ================== [{}] started success in {}s ================== ", SERVER_CONFIG.getApplicationName(), ((double) end - (double) begin) / 1000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("|||---------- Easy Rpc Starting Now! ----------|||");
        System.out.println("==============================================");
        System.out.println("version: 1.0.0");
        System.out.println();
    }
}
