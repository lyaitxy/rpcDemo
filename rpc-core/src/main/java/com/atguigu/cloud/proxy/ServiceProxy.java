package com.atguigu.cloud.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.atguigu.cloud.model.RpcRequest;
import com.atguigu.cloud.model.RpcResponse;
import com.atguigu.cloud.serializer.JdkSerializer;
import com.atguigu.cloud.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Description: 动态代理
 * @Author: LiYang
 * @Date: 2025/1/10 13:32
 */
public class ServiceProxy implements InvocationHandler {
    /**
    * @Description 调用代理
    * @params [proxy, method, args]
    * @return java.lang.Object
    */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        Serializer serializer = new JdkSerializer();

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            // TODO 这里地址被硬编码（需要使用注册中心和服务发现机制解决）
            try (HttpResponse httpResponse = HttpRequest.post("http://localhsot:8080").body(bodyBytes).execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
