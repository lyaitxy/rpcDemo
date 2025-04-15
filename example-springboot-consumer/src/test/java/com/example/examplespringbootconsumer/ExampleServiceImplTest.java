package com.example.examplespringbootconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExampleServiceImplTest {

    @Resource
    private  ExampleServiceImpl exampleService;

    private final ApplicationContext applicationContext;

    ExampleServiceImplTest(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Test
    void test1() {
        exampleService.test();
    }
}