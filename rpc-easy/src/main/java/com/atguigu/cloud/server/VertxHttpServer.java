package com.atguigu.cloud.server;

import io.vertx.core.Vertx;

/**
 * @Description: 服务器实现类
 * @Author: LiYang
 * @Date: 2025/1/9 20:29
 */
public class VertxHttpServer implements HttpServer {

    public void doStart(int port) {
        // 创建 Vert.x实例
        Vertx vertx = Vertx.vertx();

        // 创建 HTTP 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 监听端口并处理请求
        server.requestHandler(new HttpServerHandler());

        // 启动 HTTP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Vert.x HTTP server started on port " + port);
            } else {
                System.err.println("Vert.x HTTP server Failed to start server: " + result.cause());
            }
        });
    }
}
