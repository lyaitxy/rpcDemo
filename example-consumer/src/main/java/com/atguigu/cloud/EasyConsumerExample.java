package com.atguigu.cloud;

import com.atguigu.cloud.model.User;
import com.atguigu.cloud.proxy.ServiceProxyFactory;
import com.atguigu.cloud.service.UserService;

public class EasyConsumerExample {
    public static void main(String[] args) {
//         静态代理
//        UserService userService = new UserServiceProxy();
        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("llll");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}