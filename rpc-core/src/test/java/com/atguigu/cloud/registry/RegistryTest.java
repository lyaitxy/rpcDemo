package com.atguigu.cloud.registry;

import com.atguigu.cloud.config.RegistryConfig;
import org.junit.Before;

public class RegistryTest {
    final Registry registry = new EtcdRegistry();

    @Before
    public void init() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://localhost:2379");
        registry.init(registryConfig);
    }


}
