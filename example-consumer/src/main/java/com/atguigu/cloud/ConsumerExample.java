package com.atguigu.cloud;

import com.atguigu.cloud.model.User;
import com.atguigu.cloud.proxy.ServiceProxyFactory;
import com.atguigu.cloud.service.UserService;

/**
 * 简易服务消费者示例
 */
public class ConsumerExample {
    public static void main(String[] args) {

        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("llll");
        // 调用
        for(int i = 0; i < 3; i++) {
            User newUser = userService.getUser(user);
            if(newUser != null) {
                System.out.println(newUser.getName());
            } else {
                System.out.println("user == null");
            }
        }
        short number = userService.getNumber();
        System.out.println(number);
    }
}
