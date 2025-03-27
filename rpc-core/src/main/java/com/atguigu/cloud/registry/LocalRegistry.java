package com.atguigu.cloud.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 本地注册中心
 * @Author: LiYang
 * @Date: 2025/1/9 22:02
 */
public class LocalRegistry {
    /**
    * @Description 注册信息存储
    * @params
    * @return
    */
    public static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
    * @Description 注册服务
    * @params
    * @return
    */
    public static void registry(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
    * @Description 获取服务
    * @params
    * @return
    */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
    * @Description 删除服务
    * @params
    * @return
    */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
