package com.atguigu.cloud.serializer;

import java.io.IOException;

/**
* @Description 序列化器接口
* @params
* @return
*/
public interface Serializer {
    /**
    * @Description 序列化
    * @params [obj]
    * @return byte[]
    */
    <T> byte[] serialize(T obj) throws IOException;
    /**
    * @Description 反序列化
    * @params [data, clazz]
    * @return T
    */
    <T> T deserialize(byte[] data, Class<T> clazz) throws IOException;
}
