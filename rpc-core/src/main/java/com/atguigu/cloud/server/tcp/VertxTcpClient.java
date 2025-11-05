package com.atguigu.cloud.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.atguigu.cloud.RpcApplication;
import com.atguigu.cloud.model.RpcRequest;
import com.atguigu.cloud.model.RpcResponse;
import com.atguigu.cloud.model.ServiceMetaInfo;
import com.atguigu.cloud.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import jdk.internal.org.jline.utils.OSUtils;
import sun.nio.ch.Net;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Vert TCP 请求客户端
 */
public class VertxTcpClient {

    // 使用 ConcurrentHashMap 来安全地管理并发请求
    private static final ConcurrentHashMap<Long, CompletableFuture<RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();
    // 使用volatile CompletableFuture 来管理连接状态，确保多线程之间的可见性
    private static volatile CompletableFuture<NetSocket> socketFuture;
    private static final Object lock = new Object(); // 用于同步的锁对象

    // 单例的 Vertx 和 NetClient,避免重复创建
    private static final Vertx vertx = Vertx.vertx();
    private static final NetClient netClient = vertx.createNetClient();

    static {
        // 添加关闭钩子，在JVM退出时优雅关闭资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (netClient != null) {
                netClient.close();
            }
            if (vertx != null) {
                vertx.close();
            }
        }));
    }

    /**
     * 发送请求，多线程并发调用版本
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        // 如果连接尚未建立，进行等待
        NetSocket netSocket = getSocket(serviceMetaInfo);

        // 构造消息
        ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        // 生成全局请求 ID
        long requestId = rpcRequest.getRequestId();
        header.setRequestId(requestId);
        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);

        // 创建一个 Future 来等待响应
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        // 将请求ID和Future的映射存入Map
        PENDING_REQUESTS.put(requestId, responseFuture);

        // 编码并发送请求
        try {
            Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
            netSocket.write(encodeBuffer);
        } catch (IOException e) {
            // 如果发送失败，需要将对应的Future移除并抛出异常
            PENDING_REQUESTS.remove(requestId);
            throw new RuntimeException("协议消息编码或发送错误", e);
        }

        // 阻塞等待并获取结果
        // 可以在这里加超时机制
        return responseFuture.get();
    }

    private static NetSocket getSocket(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        if(socketFuture == null) {
            synchronized(lock) {
                if(socketFuture == null) {
                    init(serviceMetaInfo);
                }
            }
        }
        return socketFuture.get();
    }

    /**
     * 初始化连接（在启动应用时调用一次）
     */
    public static void init(ServiceMetaInfo serviceMetaInfo) {

        CompletableFuture<NetSocket> future = new CompletableFuture<>();

        netClient.connect(
                serviceMetaInfo.getServicePort(),
                serviceMetaInfo.getServiceHost(),
                // 获取到连接结果之后的回调
                result -> {
                   if(result.succeeded()) {
                       System.out.println("Connected to TCP server：" + serviceMetaInfo.getServiceAddress());
                       NetSocket netSocket = result.result();
                       // 设置统一的响应消息处理器，客户端是拿到服务端传递过来的消息，所以需要解码
                       netSocket.handler(new TcpBufferHandleWrapper(buffer -> {
                           try {
                               ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                               long requestId = rpcResponseProtocolMessage.getHeader().getRequestId();
                               CompletableFuture<RpcResponse> responseCompletableFuture = PENDING_REQUESTS.remove(requestId);
                               if(responseCompletableFuture != null) {
                                   responseCompletableFuture.complete(rpcResponseProtocolMessage.getBody());
                               }
                           } catch (IOException e) {
                               System.err.println("Failed to decode response: " + e.getMessage());
                               throw new RuntimeException(e);
                           }
                       }));

                       // 设置异常关闭回调（连接断开时重置）
                       netSocket.closeHandler(v -> {
                           System.out.println("Connection closed by server: " + serviceMetaInfo.getServiceAddress());
                           socketFuture = null; // 下次重新建立连接
                       });

                       netSocket.exceptionHandler(ex -> {
                           System.err.println("Socket exception: " + ex.getMessage());
                           socketFuture = null;
                       });

                       future.complete(netSocket);
                   } else {
                       System.out.println("Failed to connect to TCP server: " + result.cause());
                   }
                });

        socketFuture = future;
    }

//    /**
//     * 发送请求,旧版
//     */
//    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
//        // 发送 TCP 请求
//        Vertx vertx = Vertx.vertx();
//        NetClient netClient = vertx.createNetClient();
//        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
//        netClient.connect(
//                serviceMetaInfo.getServicePort(),
//                serviceMetaInfo.getServiceHost(),
//                result -> {
//                    if(!result.succeeded()) {
//                        System.out.println("Failed to connect to TCP server");
//                        return;
//                    }
//                    NetSocket socket = result.result();
//                    // 发送数据
//                    // 构造消息
//                    ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
//                    ProtocolMessage.Header header = new ProtocolMessage.Header();
//                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
//                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
//                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
//                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
//                    // 生成全局请求 ID
//                    header.setRequestId(IdUtil.getSnowflakeNextId());
//                    protocolMessage.setHeader(header);
//                    protocolMessage.setBody(rpcRequest);
//                    // 编码请求
//                    try {
//                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
//                        socket.write(encodeBuffer);
//                    } catch (IOException e) {
//                        throw new RuntimeException("协议消息编码错误");
//                    }
//                    // 接受响应
//                    TcpBufferHandleWrapper bufferHandleWrapper = new TcpBufferHandleWrapper(buffer -> {
//                        try {
//                            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
//                            responseFuture.complete(rpcResponseProtocolMessage.getBody());
//                        } catch (IOException e) {
//                            throw new RuntimeException("协议消息解码错误");
//                        }
//                    });
//                    // IO线程处理返回的响应
//                    socket.handler(bufferHandleWrapper);
//                });
//        // 主线程等待结果
//        RpcResponse rpcResponse = responseFuture.get();
//        // 关闭连接
//        netClient.close();
//        return rpcResponse;
//    }
}
