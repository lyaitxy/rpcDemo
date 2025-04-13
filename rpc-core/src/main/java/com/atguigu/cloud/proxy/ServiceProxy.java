package com.atguigu.cloud.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.cloud.RpcApplication;
import com.atguigu.cloud.config.RpcConfig;
import com.atguigu.cloud.constant.RpcConstant;
import com.atguigu.cloud.loadbalancer.LoadBalancer;
import com.atguigu.cloud.loadbalancer.LoadBalancerFactory;
import com.atguigu.cloud.model.RpcRequest;
import com.atguigu.cloud.model.RpcResponse;
import com.atguigu.cloud.model.ServiceMetaInfo;
import com.atguigu.cloud.protocol.*;
import com.atguigu.cloud.registry.Registry;
import com.atguigu.cloud.registry.RegistryFactory;
import com.atguigu.cloud.serializer.Serializer;
import com.atguigu.cloud.serializer.SerializerFactory;
import com.atguigu.cloud.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: 动态代理
 * @Author: LiYang
 * @Date: 2025/1/10 13:32
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {
    /**
    * @Description 调用代理
    * @params [proxy, method, args]
    * @return java.lang.Object
    */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 将请求序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);

            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            // 负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            // 将调用方法名（请求路径）作为负载均衡器参数
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            log.info("selectedServiceMetaInfo, {}", selectedServiceMetaInfo);
            // 发送 TCP 请求
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            return rpcResponse.getData();
            /*
            // 发送请求，带上自己要获取的服务和参数
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress()).body(bodyBytes).execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
