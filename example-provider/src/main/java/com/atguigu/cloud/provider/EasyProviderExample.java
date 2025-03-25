package com.atguigu.cloud.provider;

import com.atguigu.cloud.registry.LocalRegistry;
import com.atguigu.cloud.server.HttpServer;
import com.atguigu.cloud.server.VertxHttpServer;
import com.atguigu.cloud.service.UserService;

/**
 * @Description: 简易服务提供者示例
 * @Author: LiYang
 * @Date: 2025/1/9 19:28
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        //注册服务
        LocalRegistry.registry(UserService.class.getName(), UserServiceImpl.class);

        // 提供服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
