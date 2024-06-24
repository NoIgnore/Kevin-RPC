package com.kevin.rpc.common.utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

import static com.kevin.rpc.common.cache.CommonClientCache.EXTENSION_LOADER;
import static com.kevin.rpc.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class CommonUtil {

    /**
     * 获取目标对象的实现接口
     */
    public static List<Class<?>> getAllInterfaces(Class<?> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass is null!");
        }
        Class<?>[] clazz = targetClass.getInterfaces();
        if (clazz.length == 0) {
            return Collections.emptyList();
        }
        List<Class<?>> classes = new ArrayList<>(clazz.length);
        classes.addAll(Arrays.asList(clazz));
        return classes;
    }


    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (!netInterface.isLoopback() && !netInterface.isVirtual() && netInterface.isUp()) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    // 提取公共逻辑的泛型方法
    public static <T> T initializeComponent(Class<T> componentClass, String configValue) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        EXTENSION_LOADER.loadExtension(componentClass);
        LinkedHashMap<String, Class<?>> componentMap = EXTENSION_LOADER_CLASS_CACHE.get(componentClass.getName());
        Class<?> componentImplClass = componentMap.get(configValue);
        if (componentImplClass == null) {
            throw new RuntimeException("no match " + componentClass.getSimpleName() + " for " + configValue);
        }
        return componentClass.cast(componentImplClass.newInstance());
    }

    public static boolean isEmptyList(List<?> list) {
        return list == null || list.size() == 0;
    }

    public static boolean isNotEmptyList(List<?> list) {
        return !isEmptyList(list);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}