package com.hjcenry.server;

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
public enum NetServerEnum implements IndexedEnum {
    // TCP网络
    NET_TCP(0),
    // UDP网络
    NET_UDP(1),
    ;

    private final int type;

    NetServerEnum(int type) {
        this.type = type;
    }

    @Override
    public int getIndex() {
        return this.type;
    }

    private static final List<NetServerEnum> VALUES = IndexedEnumUtil.toIndexes(NetServerEnum.values());

    public static List<NetServerEnum> getValues() {
        return VALUES;
    }

    public static NetServerEnum valueOf(int value) {
        return EnumUtil.valueOf(VALUES, value);
    }

}