package com.example.rpcspringbootstarter.bootstrap;

import com.atguigu.cloud.RpcApplication;
import com.atguigu.cloud.config.RpcConfig;
import com.atguigu.cloud.server.tcp.VertxTcpServer;
import com.example.rpcspringbootstarter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Rpc 框架启动
 * ImportBeanDefinitionRegistrar接口：
 * 当你使用 @Import 注解导入某个类时，如果该类实现了 ImportBeanDefinitionRegistrar 接口，
 * Spring 会调用其 registerBeanDefinitions() 方法，在容器启动阶段向 BeanDefinitionRegistry 中动态注册 Bean 定义。
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化执行，初始化 RPC 框架
     * @param importingClassMetadata 获取某个类上注解的元信息
     * @param registry  调用BeanDefinitionRegistry方法
     */
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.info("Rpc 初始化启动类被调用了");
        // 获取 EnableRpc 注解的属性值
        boolean needServer = (boolean)importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");

        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();

        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 启动服务器
        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        } else {
            log.info("不启动 server");
        }
    }
}
