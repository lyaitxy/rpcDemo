package com.example.rpcspringbootstarter.bootstrap;

import com.atguigu.cloud.RpcApplication;
import com.atguigu.cloud.config.RegistryConfig;
import com.atguigu.cloud.config.RpcConfig;
import com.atguigu.cloud.registry.LocalRegistry;
import com.atguigu.cloud.registry.Registry;
import com.atguigu.cloud.registry.RegistryFactory;
import com.example.rpcspringbootstarter.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import com.atguigu.cloud.model.ServiceMetaInfo;

import java.util.Set;

@Slf4j
public class RpcServiceScanner implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("开始扫描 @RpcService 注解的类");

        // 创建扫描器，并配置只扫描 @RpcService 注解
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));

        // 扫描指定包（可以配置成动态的）
        String basePackage = "com.example"; // TODO: 换成你自己需要扫描的包
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(basePackage);

        for (BeanDefinition beanDefinition : beanDefinitions) {
            String className = beanDefinition.getBeanClassName();
            try {
                Class<?> beanClass = Class.forName(className);
                RpcService rpcService = beanClass.getAnnotation(RpcService.class);

                // 获取接口和服务名
                Class<?> interfaceClass = rpcService.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = beanClass.getInterfaces()[0];
                }
                String serviceName = interfaceClass.getName();
                String serviceVersion = rpcService.serviceVersion();

                // 本地注册
                LocalRegistry.registry(serviceName, beanClass);

                // 注册中心注册
                RpcConfig rpcConfig = RpcApplication.getRpcConfig();
                RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
                Registry registryInstance = RegistryFactory.getInstance(registryConfig.getRegistry());

                ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
                serviceMetaInfo.setServiceName(serviceName);
                serviceMetaInfo.setServiceVersion(serviceVersion);
                serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
                serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

                registryInstance.register(serviceMetaInfo);
                log.info("注册服务成功: {}", serviceName);

            } catch (Exception e) {
                log.error("注册服务失败: {}", className, e);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
