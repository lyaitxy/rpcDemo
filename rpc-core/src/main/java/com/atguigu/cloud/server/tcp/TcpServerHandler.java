package com.atguigu.cloud.server.tcp;

import com.atguigu.cloud.model.RpcRequest;
import com.atguigu.cloud.model.RpcResponse;
import com.atguigu.cloud.protocol.ProtocolMessage;
import com.atguigu.cloud.protocol.ProtocolMessageDecoder;
import com.atguigu.cloud.protocol.ProtocolMessageEncoder;
import com.atguigu.cloud.protocol.ProtocolMessageTypeEnum;
import com.atguigu.cloud.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        // 这个构造器中的参数是业务handler，包装一层是使用了装饰器模式，都实现了Handler接口
        TcpBufferHandleWrapper bufferHandleWrapper = new TcpBufferHandleWrapper(buffer -> {
            // 接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            // 如果请求为null， 直接返回
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                // 向连接到服务器的客户端发送数据
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
        // 处理连接，这里表示注册一个接受数据的回调函数，当客户端发来数据时，bufferHandleWrapper的handler方法被调用，经过recordParser解析半包和粘包，得到buffer参数
        netSocket.handler(bufferHandleWrapper);
        // 本来可以直接这样写的 netSocket.handler(buffer -> {})
    }
}
