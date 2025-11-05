package com.atguigu.cloud.proxy;

import com.atguigu.cloud.RpcApplication;

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
        if(RpcApplication.getRpcConfig().isMock()) {
            return getMockProxy(serviceClass);
        }
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(), // 获取服务类的类加载器
                new Class[]{serviceClass}, //代理类实现的接口
                new ServiceProxy()); // 处理器,当调用代理对象的方法时，所有调用都会被转发到这个 InvocationHandler 的 invoke() 方法中，由它决定如何处理。

    }

    private static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy()
        );
    }
}
