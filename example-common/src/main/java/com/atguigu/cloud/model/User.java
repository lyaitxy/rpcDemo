package com.atguigu.cloud.model;

import java.io.Serializable;

/**
 * @Description: 用户类
 * @Author: LiYang
 * @Date: 2025/1/9 19:19
 */
public class User implements Serializable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
