package com.atguigu.cloud.provider;

import com.atguigu.cloud.bootstrap.ProviderBootstrap;
import com.atguigu.cloud.model.ServiceRegisterInfo;
import com.atguigu.cloud.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易服务提供者示例
 */
public class ProvideExample {
    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo serviceRegisterInfo = new ServiceRegisterInfo(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }

}
