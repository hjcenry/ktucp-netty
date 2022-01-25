package com.hjcenry.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * JSON工具
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 18:14
 **/
public class JsonUtils {

    /**
     * 对象转字符串
     *
     * @param o 对象
     * @return 字符串
     */
    public static String objectToString(Object o) {
        return JSON.toJSONString(o);
    }

    /**
     * 对象转字符串
     *
     * @param o 对象
     * @return 字符串
     */
    public static String objectToStringAsStringKey(Object o) {
        return JSON.toJSONString(o, SerializerFeature.WriteNonStringKeyAsString);
    }

    public static String objectToStringWithClass(Object o) {
        return JSON.toJSONString(o, SerializerFeature.WriteClassName, SerializerFeature.IgnoreNonFieldGetter);
    }

    /**
     * 字符串转对象
     *
     * @param s     字符串
     * @param clazz 类
     * @return 对象
     */
    public static <T> T stringToObject(String s, Class<T> clazz) {
        if (s == null || "".equals(s)) {
            return null;
        }
        return JSON.parseObject(s, clazz);
    }

    /**
     * 解析对象
     *
     * @param s    字符串
     * @param type 类别
     * @return 对象
     */
    public static <T> T stringToObject(String s, TypeReference<T> type) {
        if (s == null || "".equals(s)) {
            return null;
        }
        return JSON.parseObject(s, type);
    }

    /**
     * 解析 list
     *
     * @param s     字符串
     * @param clazz 类
     * @param <T>   泛型
     * @return list
     */
    public static <T> List<T> stringToList(String s, Class<T> clazz) {
        if (s == null || "".equals(s)) {
            return null;
        }
        return JSON.parseArray(s, clazz);
    }


}
