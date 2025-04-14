package com.atguigu.cloud.config;

import com.atguigu.cloud.fault.retry.RetryStrategyKeys;
import com.atguigu.cloud.loadbalancer.LoadBalancerKeys;
import com.atguigu.cloud.serializer.SerializerKeys;
import lombok.Data;

/**
 *  RPC 框架配置
 */
@Data
public class RpcConfig {
    /**
     * 名称
     */
    private String name = "rpc";
    /**
     * 版本号
     */
    private String version = "1.0";
    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";
    /**
     * 服务器端口号
     */
    private Integer serverPort = 8081;
    /**
     * 模拟调用
     */
    private boolean mock = false;
    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.KRYO;
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.CONSISTENT_HASH;
    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;
}
