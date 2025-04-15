package com.example.examplespringbootprovider;

import com.atguigu.cloud.model.User;
import com.atguigu.cloud.service.UserService;
import com.example.rpcspringbootstarter.annotation.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名: " + user.getName());
        return user;
    }
}
