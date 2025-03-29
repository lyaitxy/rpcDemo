package com.atguigu.cloud.provider;

import com.atguigu.cloud.model.User;
import com.atguigu.cloud.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 用户服务实现类
 * @Author: LiYang
 * @Date: 2025/1/9 19:27
 */
@Slf4j
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名:" + user.getName());
        log.info("userService的具体实现类触发了");
        return user;
    }
}
