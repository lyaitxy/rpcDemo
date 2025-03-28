package com.atguigu.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: RPC 请求
 * @Author: LiYang
 * @Date: 2025/1/9 22:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    // 服务名称
    private String serviceName;
    // 方法名称
    private String methodName;
    // 参数类型列表
    private Class<?>[] parameterTypes;
    // 参数列表
    private Object[] args;
}
