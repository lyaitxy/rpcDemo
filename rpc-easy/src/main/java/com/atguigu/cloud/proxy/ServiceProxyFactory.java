package com.atguigu.cloud.proxy;

import java.lang.reflect.Proxy;

/**
 * @Description: 代理工厂(用于创建代理对象)
 * @Author: LiYang
 * @Date: 2025/1/10 13:40
 */
public class ServiceProxyFactory {
    /**
    * @Description 根据服务类获取代理对象
    * @params
    * @return
    */
    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());

    }
}
