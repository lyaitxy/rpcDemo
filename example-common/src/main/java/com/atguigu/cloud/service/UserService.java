package com.atguigu.cloud.service;

import com.atguigu.cloud.model.User;

/**
 * @Description: 用户服务
 * @Author: LiYang
 * @Date: 2025/1/9 19:21
 */
public interface UserService {

    /**
    * @Description 获取用户
    * @params
    * @return
    */
    User getUser(User user);
}
