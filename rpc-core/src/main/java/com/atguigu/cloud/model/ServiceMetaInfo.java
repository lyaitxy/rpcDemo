package com.atguigu.cloud.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 服务元信息，包含服务名称，服务地址，调用的服务地址和调用的服务端口
 */
@Data
public class ServiceMetaInfo {

    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务版本号
     */
    private String serviceVersion = "1.0";
    /**
     * 服务地址
     */
    private String serviceAddress;
    /**
     * 服务分组（暂未实现）
     */
    private String serviceGroup = "default";
    /**
     * 调用的服务地址
     */
    private String serviceHost;
    /**
     * 调用的服务端口
     */
    private Integer servicePort;

    /**
     * 获取服务键名，组成为 服务名称:服务版本号, serviceName:serviceVersion
     * @return
     */
    public String getServiceKey() {
        // 后续可拓展服务分组
//        return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务注册节点键名，组成为 服务名称:服务版本号/服务地址
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s", getServiceKey(), serviceAddress);
    }

    /**
     * 获取完整服务地址, 组成为 调用的服务地址:调用的服务端口
     * @return
     */
    public String getServiceAddress() {
//        if (!StrUtil.contains(serviceHost, "http")) {
//            return String.format("http://%s:%s", serviceHost, servicePort);
//        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
