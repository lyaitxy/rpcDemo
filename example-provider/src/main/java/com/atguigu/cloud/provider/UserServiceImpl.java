package com.atguigu.cloud.provider;

import com.atguigu.cloud.model.User;
import com.atguigu.cloud.service.UserService;

/**
 * @Description: 用户服务实现类
 * @Author: LiYang
 * @Date: 2025/1/9 19:27
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名:" + user.getName());
        return user;
    }
}
