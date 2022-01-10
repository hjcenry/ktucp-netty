package com.hjcenry.util.enumutil;

import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 从0开始的可索引枚举接口定义，实现此接口的枚举各个元素的index可以不连续，但此接口的实现类多为稀疏数组结构，保持index连续可以节省空间
 *
 * @author guowei
 * @since 2010-6-7
 */
public interface IndexedEnum {

    static final Logger logger = LoggerFactory.getLogger(IndexedEnum.class);

    /**
     * 获取该枚举的索引值
     *
     * @return 返回>=0的索引值
     */
    public abstract int getIndex();

    public static class IndexedEnumUtil {

        /**
         * 索引警戒上限，发现超过此值的索引可能存在较大的空间浪费
         */
        private static final int WORNNING_MAX_INDEX = 1000;

        /**
         * 检测枚举索引
         * <p>
         * 仅在起服时候用
         *
         * @return 如果未定义，返回false。
         */
        public static <T extends Enum<T>> boolean checkIndex(List<T> allIndexedEnums, int index) {
            return checkIndex(allIndexedEnums, index, true);
        }

        /**
         * 检测枚举索引
         * <p>
         * 仅在起服时候用
         *
         * @return 如果未定义，返回false。
         */
        public static <T extends Enum<T>> boolean checkIndex(List<T> allIndexedEnums, int index, boolean printErrLog) {
            try {
                if (EnumUtil.valueOf(allIndexedEnums, index, printErrLog) == null) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        /**
         * 将枚举中的元素放到一个List中，每个元素在list中的下表即为他的index，如果有不连续的index，则空缺的index用null填充
         *
         * @param enums
         * @param <E>
         * @return
         */
        public static <E extends IndexedEnum> List<Integer> toIndexIntList(E[] enums) {
            int maxIndex = Integer.MIN_VALUE;
            int curIdx = 0;
            // 找到最大index，此值+1就是结果list的size
            for (E enm : enums) {
                curIdx = enm.getIndex();
                // 索引不能为负
                Assert.isTrue(curIdx >= 0, String.format("枚举索引不能为负 index: %1$d type: %2$s ", curIdx, enums.getClass()
                        .getComponentType().getName()));
                if (curIdx > maxIndex) {
                    maxIndex = curIdx;
                }
            }
            if (maxIndex >= WORNNING_MAX_INDEX) {
                logger.warn(String.format("警告：枚举类%s中有索引超过%d的索引，如果有很多索引空缺，可能会造成空间浪费", enums.getClass().getSimpleName(), WORNNING_MAX_INDEX));
            }
            List<Integer> instances = new ArrayList<>(maxIndex + 1);
            // 先全用null填充
            for (int i = 0; i < maxIndex + 1; i++) {
                instances.add(null);
            }
            for (E enm : enums) {
                curIdx = enm.getIndex();
                // 索引必须唯一
                Assert.isTrue(instances.get(curIdx) == null, String.format("枚举中有重复的index type=%s,index=%s "
                        , enums.getClass().getComponentType().getName(), curIdx));
                instances.set(curIdx, enm.getIndex());
            }
            return instances;
        }

        /**
         * 将枚举中的元素放到一个List中，每个元素在list中的下表即为他的index，如果有不连续的index，则空缺的index用null填充
         *
         * @param <E>
         * @param enums
         * @return
         */
        public static <E extends IndexedEnum> List<E> toIndexes(E[] enums) {
            int maxIndex = Integer.MIN_VALUE;
            int curIdx = 0;
            // 找到最大index，此值+1就是结果list的size
            for (E enm : enums) {
                curIdx = enm.getIndex();
                // 索引不能为负
                Assert.isTrue(curIdx >= 0, String.format("枚举索引不能为负 index: %1$d type: %2$s ", curIdx, enums.getClass()
                        .getComponentType().getName()));
                if (curIdx > maxIndex) {
                    maxIndex = curIdx;
                }
            }
            if (maxIndex >= WORNNING_MAX_INDEX) {
                logger.warn(String.format("警告：枚举类%s中有索引超过%d的索引，如果有很多索引空缺，可能会造成空间浪费", enums.getClass().getSimpleName(), WORNNING_MAX_INDEX));
            }
            List<E> instances = new ArrayList<E>(maxIndex + 1);
            // 先全用null填充
            for (int i = 0; i < maxIndex + 1; i++) {
                instances.add(null);
            }
            for (E enm : enums) {
                curIdx = enm.getIndex();
                // 索引必须唯一
                Assert.isTrue(instances.get(curIdx) == null, String.format("枚举中有重复的index type=%s,index=%s "
                        , enums.getClass().getComponentType().getName(), curIdx));
                instances.set(curIdx, enm);
            }
            return instances;
        }

        /**
         * 将枚举中的元素放到一个List中，每个元素在list中的下表即为他的index，如果有不连续的index，则空缺的index用null填充
         *
         * @param <E>
         * @param enums
         * @return
         */
        public static <E extends IndexedEnum> List<E> toReverseIndexes(E[] enums) {
            int maxIndex = Integer.MIN_VALUE;
            int curIdx = 0;
            // 找到最大index，此值+1就是结果list的size
            for (E enm : enums) {
                curIdx = enm.getIndex();
                // 索引不能为负
                Assert.isTrue(curIdx >= 0, String.format("枚举索引不能为负 index: %1$d type: %2$s ", curIdx, enums.getClass()
                        .getComponentType().getName()));
                if (curIdx > maxIndex) {
                    maxIndex = curIdx;
                }
            }
            if (maxIndex >= WORNNING_MAX_INDEX) {
                logger.warn(String.format("警告：枚举类%s中有索引超过%d的索引，如果有很多索引空缺，可能会造成空间浪费", enums.getClass().getSimpleName(), WORNNING_MAX_INDEX));
            }
            List<E> instances = new ArrayList<E>(maxIndex + 1);
            // 先全用null填充
            for (int i = 0; i < maxIndex + 1; i++) {
                instances.add(null);
            }
            int reverseIndex;
            for (E enm : enums) {
                curIdx = enm.getIndex();
                reverseIndex = maxIndex - curIdx;
                // 索引必须唯一
                Assert.isTrue(instances.get(reverseIndex) == null, "枚举中有重复的index type= "
                        + enums.getClass().getComponentType().getName());
                instances.set(reverseIndex, enm);
            }
            return instances;
        }

        public static <E extends IndexedEnum> HashMap<Integer, E> toIndexMap(E[] enums) {
            HashMap<Integer, E> map = new HashMap<>();
            for (E enm : enums) {
                map.put(enm.getIndex(), enm);
            }
            return map;
        }

        /**
         * 打印枚举所有index
         *
         * @param allIndexedEnums
         * @param <E>
         * @return
         */
        public static <E extends IndexedEnum> String printEnumIndexes(List<E> allIndexedEnums) {
            StringBuilder stringBuilder = new StringBuilder();
            for (E e : allIndexedEnums) {
                if (e == null) {
                    continue;
                }
                stringBuilder.append(e.getIndex() + " ");
            }
            return stringBuilder.length() > 0 ? stringBuilder.substring(0, stringBuilder.length() - 1) : stringBuilder.toString();
        }
    }
}