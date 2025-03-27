package com.atguigu.cloud;

import com.atguigu.cloud.config.RpcConfig;
import com.atguigu.cloud.utils.ConfigUtils;

/**
 * 简易服务消费者示例
 */
public class ConsumerExample {
    public static void main(String[] args) {

        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);

    }
}
