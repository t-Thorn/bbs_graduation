package com.thorn.bbsmain.utils.shiro;

import lombok.Cleanup;

import java.io.*;

/**
 * Serializable工具(JDK)(也可以使用Protobuf自行百度)
 *
 * @author Wang926454
 */
public class SerializableUtil {

    /**
     * 序列化
     *
     * @param object
     * @return byte[]
     * @author Wang926454
     */
    public static byte[] serializable(Object object) throws Exception {
        @Cleanup ByteArrayOutputStream baos = null;
        @Cleanup ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("SerializableUtil工具类序列化出现IOException异常");
        }
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return java.lang.Object
     * @author Wang926454
     */
    public static Object unserializable(byte[] bytes) throws Exception {
        @Cleanup ByteArrayInputStream bais = null;
        @Cleanup ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception("SerializableUtil工具类反序列化出现ClassNotFoundException异常");
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("SerializableUtil工具类反序列化出现IOException异常");
        }
    }
}
