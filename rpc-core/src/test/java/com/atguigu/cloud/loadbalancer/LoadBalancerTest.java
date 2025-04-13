package com.atguigu.cloud.loadbalancer;

import com.atguigu.cloud.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 负载均衡器测试
 */
public class LoadBalancerTest {

    final LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();

    @Test
    public void select() {
        // 请求参数
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", "apple");
        // 服务列表
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("1111");
        serviceMetaInfo.setServicePort(80);
        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo, serviceMetaInfo1);
        // 连续调用 3 次
        ServiceMetaInfo selected = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(selected);
        Assert.assertNotNull(selected);
        selected = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(selected);
        Assert.assertNotNull(selected);
        selected = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(selected);
        Assert.assertNotNull(selected);
    }
}