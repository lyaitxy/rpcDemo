package com.atguigu.cloud.server.tcp;

import com.atguigu.cloud.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

public class TcpBufferHandleWrapper implements Handler<Buffer> {

    // 这个类的原理是 缓冲（存在一个缓冲区） + 状态机（解析模式分为固定长度模式和分隔符模式） + 分块输出（按照模式在缓冲区读到了一个或多个完整的块，就会执行回调函数）
    private final RecordParser recordParser;

    public TcpBufferHandleWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 构造parser，初始固定读取消息头
        // 设置parser为固定长度模式，先读取一个完整的消息头
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        // 读取了一个完整的消息头后，才会执行以下代码，回调对象，匿名内部类，
        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 一次完整的读取（头 + 体）
            Buffer resultBuffer = Buffer.buffer();
            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度,消息头的第13个字节是消息体长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 写入头信息到结果
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 写入体信息拼接到头消息
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整 Buffer，执行处理
                    bufferHandler.handle(resultBuffer);
                    // 重置一轮
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }


    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
