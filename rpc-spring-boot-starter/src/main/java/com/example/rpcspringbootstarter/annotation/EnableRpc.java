package com.example.rpcspringbootstarter.annotation;

import com.example.rpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.example.rpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.example.rpcspringbootstarter.bootstrap.RpcServiceScanner;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 Rpc 注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcServiceScanner.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动 server
     */
    boolean needServer() default true;
}
