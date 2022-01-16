package com.hjcenry.util;

/**
 * 断言工具类，用于对方法的传入参数进行校验，如果未通过则
 * 抛出<code>IllegalArgumentException</code>异常
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/16 10:29
 **/
public class Assert {
    /**
     * 断言对象不为空
     *
     * @param obj
     */
    public static void notNull(Object obj) {
        if (obj == null) {
            notNull(obj, null);
        }
    }

    /**
     * 断言对象不为空
     *
     * @param obj
     */
    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 断言表达式为真
     *
     * @param expression
     */
    public static void isTrue(boolean expression) {
        if (!expression) {
            isTrue(expression, null);
        }
    }

    /**
     * 断言表达式为真
     *
     * @param expression
     */
    public static void isTrue(boolean expression, String msg) {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 断言类型相同
     *
     * @param obj
     * @param clz
     * @param msg
     */
    public static void instanceOf(Object obj, Class<?> clz, String msg) {
        if (!clz.isInstance(obj)) {
            throw new IllegalArgumentException(msg);
        }
    }
}
