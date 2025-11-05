package com.atguigu.cloud.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.atguigu.cloud.config.RegistryConfig;
import com.atguigu.cloud.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 1. kvClient：用于对 etcd 中的键值对进行操作。通过 kvClient 可以进行设置值、获取值、删除
 * 值、列出目录等操作。
 * 2. leaseClient：用于管理 etcd 的租约机制。租约是 etcd 中的一种时间片，用于为键值对分配
 * 生存时间，并在租约到期时自动删除相关的键值对。通过 leaseClient 可以创建、获取、续约
 * 和撤销租约。
 * 3. watchClient：用于监视 etcd 中键的变化，并在键的值发生变化时接收通知。
 * 4. clusterClient：用于与 etcd 集群进行交互，包括添加、移除、列出成员、设置选举、获取集
 * 群的健康状态、获取成员列表信息等操作。
 * 5. authClient：用于管理 etcd 的身份验证和授权。通过 authClient 可以添加、删除、列出用
 * 户、角色等身份信息，以及授予或撤销用户或角色的权限。
 * 6. maintenanceClient：用于执行 etcd 的维护操作，如健康检查、数据库备份、成员维护、数
 * 据库快照、数据库压缩等。
 * 7. lockClient：用于实现分布式锁功能，通过 lockClient 可以在 etcd 上创建、获取、释放锁，
 * 能够轻松实现并发控制。
 * 8. electionClient：用于实现分布式选举功能，可以在 etcd 上创建选举、提交选票、监视选举结
 * 果等。
 */
public class EtcdRegistry implements Registry{

    private Client client;

    private KV kvClient;

    // 本机注册的节点 key 集合（用于续期）
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 缓存的是一个服务对应的所有的服务提供者信息，正常情况下，服务信息列表的更新频率是不高的，所以使用缓存，能够提高性能
     */
//    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    private final RegistryMultiServiceCache registryMultiServiceCache = new RegistryMultiServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        // 开启心跳检测
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建一个30秒的租约
        long leaseId = leaseClient.grant(30).get().getID();
        // 设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8); // 服务元信息json化作为value

        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        // 从本地缓存移除
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 返回一个列表，是因为该服务可能多个节点都提供了
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {

        // 优先从缓存获取服务
//        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryMultiServiceCache.readCache(serviceKey);
        if(cachedServiceMetaInfoList != null) return cachedServiceMetaInfoList;

        // 前缀搜索，结尾一定要加 '/'，为什么是前缀搜索，因为key没有带服务地址和端口
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            // 根据前缀key，搜索到对应的键值对
            List<KeyValue> keyValues = kvClient.get(
                    ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                    getOption).
                    get().
                    getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream().map(keyValue -> {
                String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                // 监听key的变化
                watch(key);
                String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(value, ServiceMetaInfo.class);
            }).collect(Collectors.toList());

            // 写入缓存服务
//            registryServiceCache.writeCache(serviceMetaInfoList);
            registryMultiServiceCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    /**
     * 当前节点所有的注册服务主动下线
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        // 遍历本节点的所有的key
        for(String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if(client != null) {
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        // 每10秒续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的 key，只需要管理本节点注册的key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                // 阻塞调用，等待获取etcd返回结果
                                .get()
                                // 获取对应 key 的值列表，虽然一般一个 key 只对应一个value
                                .getKvs();
                        // 如果该节点已过期（需要重启节点才能重新注册）
                        if (CollUtil.isEmpty(keyValues)) continue;
                        // 节点未过期，重新注册（相当于续签
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if(newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // key 删除时触发
                        case DELETE:
                            // 清理注册服务缓存
//                            registryServiceCache.clearCache();
                            String[] split = serviceNodeKey.split("/");
                            registryMultiServiceCache.clearCache(split[2]+ ":" + split[3]);
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }
}
