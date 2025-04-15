package com.example.examplespringbootconsumer;

import com.atguigu.cloud.model.User;
import com.atguigu.cloud.service.UserService;
import com.example.rpcspringbootstarter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {

    @RpcReference
    private UserService userService;

    public void test() {
        User user = new User();
        user.setName("llllyyyyy");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }
}
