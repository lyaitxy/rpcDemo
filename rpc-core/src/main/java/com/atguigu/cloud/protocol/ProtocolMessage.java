package com.atguigu.cloud.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议消息结构，一般RPC框架注重性能，使用最少的空间传递需要的信息，
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {

    /**
     * 消息头
     */
    private Header header;
    /**
     * 消息体（请求或响应对象）
     */
    private T body;
    /**
     * 协议消息头
     */
    @Data
    public static class Header {

        /**
         * 魔数，保证安全性，防止服务器处理了非框架发来的乱七八糟的消息
         */
        private byte magic;
        /**
         * 版本号，保证请求和响应的一致性（类似 HTTP 协议有1.0 / 2.0等版本）
         */
        private byte version;
        /**
         * 序列化器，来告诉服务端和客户端如何解析数据
         */
        private byte serializer;
        /**
         * 消息类型（请求 / 响应）
         */
        private byte type;
        /**
         * 状态,如果是响应，记录响应的结果（类似HTTP的200状态代码）
         */
        private byte status;
        /**
         * 请求id，唯一标识某个请求，因为TCP是双向通信的，需要有一个唯一标识来追踪每个请求
         */
        private long requestId;
        /**
         * 消息体长度
         */
        private int bodyLength;
    }
}
