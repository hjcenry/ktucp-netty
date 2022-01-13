package com.hjcenry.net.server;

import com.hjcenry.util.enumutil.EnumUtil;
import com.hjcenry.util.enumutil.IndexedEnum;

import java.util.List;

/**
 * 网络类型
 *
 * @ClassName NetMessageType
 * @Author hejincheng
 * @Date 2021/12/30 11:33
 * @Version 1.0
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