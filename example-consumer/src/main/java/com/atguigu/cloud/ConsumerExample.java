package com.atguigu.cloud;

import com.atguigu.cloud.bootstrap.ConsumerBootstrap;
import com.atguigu.cloud.model.User;
import com.atguigu.cloud.proxy.ServiceProxyFactory;
import com.atguigu.cloud.service.UserService;

/**
 * 简易服务消费者示例
 */
public class ConsumerExample {
    public static void main(String[] args) {

        // 服务提供者初始化
        ConsumerBootstrap.init();

        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("llll");
        // 调用
        for(int i = 0; i < 3; i++) {
            long before = System.currentTimeMillis();
            User newUser = userService.getUser(user);
            long after = System.currentTimeMillis();
            System.out.printf("花费多少毫秒: %d\n", after - before);
            if(newUser != null) {
                System.out.println(newUser.getName());
            } else {
                System.out.println("user == null");
            }
        }
    }
}
