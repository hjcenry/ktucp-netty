package com.hjcenry.util.enumutil;

import com.hjcenry.server.AbstractNetServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.util.List;

/**
 * 枚举工具
 *
 * @author guowei
 * @since 2010-4-21
 */
public class EnumUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EnumUtil.class);

    /**
     * 根据枚举index返回枚举元素，index从0开始
     *
     * @param <T>    枚举类型
     * @param values 枚举元素输注
     * @param index  从0开始的index
     * @return 枚举元素
     */
    public static <T extends Enum<T>> T valueOf(List<T> values, int index) {
        return valueOf(values, index, true);
    }

    /**
     * 根据枚举index返回枚举元素，index从0开始
     *
     * @param <T>         枚举类型
     * @param values      枚举元素输注
     * @param index       从0开始的index
     * @param printErrLog 是否打印错误日志
     * @return 枚举元素
     */
    public static <T extends Enum<T>> T valueOf(List<T> values, int index, boolean printErrLog) {
        T value = null;
        try {
            value = values.get(index);
        } catch (Exception e) {
            String typeName = "unknown";
            if (values != null) {
                for (T enu : values) {
                    if (enu != null) {
                        typeName = enu.getClass().getName();
                        break;
                    }
                }
            }
            if (printErrLog) {
                LOG.error(String.format("从枚举中取元素时错误 type=%1$s index=%2$d", typeName, index), e);
            }
            return null;
        }
        return value;
    }
}
