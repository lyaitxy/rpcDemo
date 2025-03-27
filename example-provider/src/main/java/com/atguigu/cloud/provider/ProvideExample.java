package com.atguigu.cloud.provider;

import com.atguigu.cloud.RpcApplication;
import com.atguigu.cloud.registry.LocalRegistry;
import com.atguigu.cloud.server.VertxHttpServer;
import com.atguigu.cloud.service.UserService;

/**
 * 简易服务提供者示例
 */
public class ProvideExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        LocalRegistry.registry(UserService.class.getName(), UserServiceImpl.class);

        // 启动web服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
