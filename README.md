# RPC 框架

一个基于 Java 的轻量级 RPC（Remote Procedure Call）框架，支持服务注册发现、负载均衡、容错重试、多种序列化方式等核心功能。

## 📚 项目简介

本项目实现了一个完整的 RPC 框架，支持分布式服务的远程调用。消费者通过服务接口名、方法和参数构造 `RpcRequest`，经过序列化后发送到注册中心找到的服务地址；提供者监听端口，接收请求后反序列化并调用本地服务，将 `RpcResponse` 序列化后返回给消费者。

### 核心特性

- ✅ **服务注册与发现**：支持 Etcd、ZooKeeper 作为注册中心
- ✅ **多种序列化方式**：JDK、JSON、Kryo、Hessian
- ✅ **负载均衡**：随机、轮询、一致性哈希
- ✅ **容错机制**：重试策略、失败降级
- ✅ **自定义协议**：高效的二进制通信协议
- ✅ **SPI 扩展机制**：灵活的插件化架构
- ✅ **Spring Boot 支持**：注解驱动，开箱即用

## 🏗️ 项目架构

### 模块说明

| 模块                          | 说明                                           |
| ----------------------------- | ---------------------------------------------- |
| `rpc-core`                    | 核心框架模块，包含配置、SPI、协议、负载均衡等  |
| `rpc-easy`                    | 简化版 RPC 实现，用于学习理解                  |
| `rpc-spring-boot-starter`     | Spring Boot 启动器，提供注解式服务注册和调用   |
| `example-common`              | 公共服务接口和模型定义                         |
| `example-provider`            | 服务提供者示例（原生方式）                     |
| `example-consumer`            | 服务消费者示例（原生方式）                     |
| `example-springboot-provider` | 服务提供者示例（Spring Boot 注解方式）         |
| `example-springboot-consumer` | 服务消费者示例（Spring Boot 注解方式）         |

### 核心组件

#### 1. 注册中心 (Registry)
- **实现**：Etcd、ZooKeeper
- **功能**：服务注册、服务发现、心跳检测、服务缓存
- **扩展**：基于 SPI 机制，可自定义注册中心实现

#### 2. 序列化器 (Serializer)
- **支持类型**：
  - JDK 序列化
  - JSON（Jackson）
  - Kryo（高性能）
  - Hessian（跨语言）
- **配置方式**：通过 `rpc.serializer` 指定

#### 3. 负载均衡 (LoadBalancer)
- **策略**：
  - `RandomLoadBalancer`：随机选择
  - `RoundRobinLoadBalancer`：轮询
  - `ConsistentHashLoadBalancer`：一致性哈希
- **扩展**：实现 `LoadBalancer` 接口并注册 SPI

#### 4. 容错机制 (Fault Tolerance)
- **重试策略** (`RetryStrategy`)：支持失败重试，可配置重试次数
- **容错策略** (`TolerantStrategy`)：
  - `FailBackTolerantStrategy`：失败降级

#### 5. 自定义协议 (Protocol)
- **协议常量**：`ProtocolConstant`
- **消息类型**：`ProtocolMessageTypeEnum`
- **消息状态**：`ProtocolMessageStatusEnum`
- **编解码器**：`ProtocolMessageEncoder`、`ProtocolMessageDecoder`

#### 6. 网络通信
- **服务端**：基于 Vert.x 的 `VertxHttpServer`
- **请求处理**：`HttpServerHandler` 处理 RPC 请求

## 🚀 快速开始

### 环境要求

- JDK 8+
- Maven 3.x
- Etcd 或 ZooKeeper（注册中心）

### 方式一：原生 API 使用

#### 1. 定义服务接口（example-common）

```java
public interface UserService {
    User getUserById(Long id);
}
```

#### 2. 服务提供者（example-provider）

```java
public class ProviderExample {
    public static void main(String[] args) {
        // 框架初始化
        RpcApplication.init();
        
        // 本地服务注册
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        
        // 注册到注册中心
        RegistryConfig registryConfig = RpcApplication.getRpcConfig().getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost(RpcApplication.getRpcConfig().getServerHost());
        serviceMetaInfo.setServicePort(RpcApplication.getRpcConfig().getServerPort());
        registry.register(serviceMetaInfo);
        
        // 启动 Web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
```

#### 3. 服务消费者（example-consumer）

```java
public class ConsumerExample {
    public static void main(String[] args) {
        // 获取代理对象
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        
        // 调用服务
        User user = userService.getUserById(1L);
        System.out.println(user);
    }
}
```

### 方式二：Spring Boot 注解方式

#### 1. 引入依赖

```xml
<dependency>
    <groupId>com.atguigu.cloud</groupId>
    <artifactId>rpc-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

#### 2. 服务提供者

```java
@SpringBootApplication
@EnableRpc(needServer = true)
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}

@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Long id) {
        // 业务逻辑
        return new User(id, "张三");
    }
}
```

#### 3. 服务消费者

```java
@SpringBootApplication
@EnableRpc(needServer = false)
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}

@Service
public class ExampleServiceImpl {
    @RpcReference
    private UserService userService;
    
    public void test() {
        User user = userService.getUserById(1L);
        System.out.println(user);
    }
}
```

#### 4. 配置文件（application.properties）

```properties
# RPC 配置
rpc.name=my-rpc
rpc.version=1.0
rpc.serverHost=localhost
rpc.serverPort=8080
rpc.serializer=kryo

# 注册中心配置
rpc.registryConfig.registry=etcd
rpc.registryConfig.address=http://localhost:2379
rpc.registryConfig.timeout=10000
```

## 📖 工作原理

### 服务提供者启动流程

1. **框架初始化**（`RpcApplication.init()`）
   - 加载配置文件（`application.properties`）
   - 若无配置则使用默认配置

2. **注册中心初始化**
   - 通过工厂模式获取注册中心实例
   - 触发 SPI 机制加载注册中心实现
   - 初始化 client 和 kvClient（以 Etcd 为例）

3. **服务注册**
   - **本地注册**：将服务接口与实现类的映射关系存储到 `LocalRegistry`
   - **远程注册**：
     - 构造服务元信息 `ServiceMetaInfo`
     - 创建租约（lease）
     - 注册到注册中心，key 格式：`/rpc/{serviceName}:{version}/{serviceAddress}`

4. **启动 Web 服务**
   - 创建 `VertxHttpServer` 并启动
   - 注册 `HttpServerHandler` 处理请求
   - 通过 SPI 加载序列化器处理请求/响应

### 服务消费者调用流程

1. **获取服务代理**（`ServiceProxyFactory.getProxy()`）
   - 创建动态代理对象，绑定 `ServiceProxy` 调用处理器

2. **服务发现**
   - 从注册中心获取服务列表
   - 使用缓存机制减少注册中心访问

3. **负载均衡**
   - 根据配置的策略选择一个服务实例

4. **发起调用**
   - 构造 `RpcRequest` 并序列化
   - 通过自定义协议发送请求
   - 支持重试和容错降级

5. **处理响应**
   - 接收 `RpcResponse` 并反序列化
   - 返回调用结果

## 🔧 配置说明

### 默认配置

```java
name = "rpc"
version = "1.0"
serverHost = "localhost"
serverPort = 8081
isMock = false
serializer = "kryo"
registryConfig = 默认配置
```

### SPI 配置文件路径

```
META-INF/rpc/system/
├── com.atguigu.cloud.serializer.Serializer
├── com.atguigu.cloud.registry.Registry
├── com.atguigu.cloud.loadbalancer.LoadBalancer
├── com.atguigu.cloud.fault.retry.RetryStrategy
└── com.atguigu.cloud.fault.tolerant.TolerantStrategy
```

## 🛠️ 技术栈

- **核心框架**：Vert.x、Hutool
- **注册中心**：Etcd（jetcd-core）、ZooKeeper（curator）
- **序列化**：Kryo、Hessian、Jackson
- **重试机制**：Guava Retrying
- **日志**：Logback
- **构建工具**：Maven

## 📝 注解说明

| 注解            | 作用                     | 属性                |
| --------------- | ------------------------ | ------------------- |
| `@EnableRpc`    | 启用 RPC 功能            | `needServer`        |
| `@RpcService`   | 标注服务提供者           | 服务接口、版本等    |
| `@RpcReference` | 注入 RPC 服务代理        | 服务接口、版本等    |

## 🎯 扩展开发

### 自定义序列化器

1. 实现 `Serializer` 接口
2. 在 `META-INF/rpc/system/com.atguigu.cloud.serializer.Serializer` 添加配置
3. 配置文件中指定序列化器名称

### 自定义注册中心

1. 实现 `Registry` 接口
2. 在 SPI 配置文件中注册
3. 配置文件中指定注册中心类型

## 📄 许可证

本项目仅供学习交流使用。

