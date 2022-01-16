package com.hjcenry.kcp;

import com.hjcenry.util.enumutil.EnumUtil;
import com.hjcenry.util.enumutil.IndexedEnum;

import java.util.List;

/**
 * 网络类型
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/16 10:19
 **/
public enum NetTypeEnum implements IndexedEnum {
    // TCP网络
    NET_TCP(0),
    // UDP网络
    NET_UDP(1),
    ;

    private final int type;

    NetTypeEnum(int type) {
        this.type = type;
    }

    @Override
    public int getIndex() {
        return this.type;
    }

    private static final List<NetTypeEnum> VALUES = IndexedEnumUtil.toIndexes(NetTypeEnum.values());

    public static List<NetTypeEnum> getValues() {
        return VALUES;
    }

    public static NetTypeEnum valueOf(int value) {
        return EnumUtil.valueOf(VALUES, value);
    }

}