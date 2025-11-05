package com.atguigu.cloud.fault.tolerant;

import cn.hutool.core.collection.CollUtil;
import com.atguigu.cloud.loadbalancer.LoadBalancer;
import com.atguigu.cloud.model.RpcRequest;
import com.atguigu.cloud.model.RpcResponse;
import com.atguigu.cloud.model.ServiceMetaInfo;
import com.atguigu.cloud.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 转移到其他服务节点 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 获取其他服务节点并调用
        // 1. 从上下文中解析所需参数
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceMetaInfoList");
        ServiceMetaInfo failedService = (ServiceMetaInfo) context.get("failedService");
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        LoadBalancer loadBalancer = (LoadBalancer) context.get("loadBalancer");
        Map<String, Object> requestParams = (Map<String, Object>) context.get("requestParams");

        // 校验参数
        if (CollUtil.isEmpty(serviceMetaInfoList) || failedService == null || rpcRequest == null || loadBalancer == null) {
            log.error("FailOver context is incomplete. Cannot perform failover.");
            return RpcResponse.builder()
                    .message("Failover failed: context is incomplete.")
                    .exception(new RuntimeException("Failover context incomplete", e))
                    .build();
        }

        // 2. 从服务列表中移除失败的节点
        // 创建一个新列表，不修改原始列表
        List<ServiceMetaInfo> remainingServices = serviceMetaInfoList.stream()
                .filter(service -> !service.getServiceKey().equals(failedService.getServiceKey()) || !service.getServiceAddress().equals(failedService.getServiceAddress()))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(remainingServices)) {
            log.error("No remaining services to failover to.");
            return RpcResponse.builder()
                    .message("Failover failed: No remaining services.")
                    .exception(new RuntimeException("No remaining services for failover", e))
                    .build();
        }

        // 3. 使用负载均衡器选择一个新的节点
        log.info("Attempting failover. Remaining services count: {}", remainingServices.size());
        ServiceMetaInfo newSelectedService = loadBalancer.select(requestParams, remainingServices);
        log.info("Failover target selected: {}", newSelectedService.getServiceAddress());

        try {
            // 4. 发起调用 (注意：这里不再包裹重试策略，故障转移只尝试一次)
            // 如果你希望故障转移也支持重试，那么逻辑会更复杂，
            // 你需要从 context 中获取 RetryStrategy 或 RpcConfig 来重新创建它。
            // 通常，故障转移是一次性的尝试。
            return VertxTcpClient.doRequest(rpcRequest, newSelectedService);
        } catch (Exception ex) {
            log.error("Failover attempt to service {} failed: {}", newSelectedService.getServiceAddress(), ex.getMessage());
            // 故障转移也失败了，返回最终的失败响应
            return RpcResponse.builder()
                    .message("Failover attempt failed.")
                    .exception(ex) // 包装新的异常
                    .build();
        }
    }
}
