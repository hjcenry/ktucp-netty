package com.hjcenry.test;

import com.hjcenry.util.enumutil.EnumUtil;
import com.hjcenry.util.enumutil.IndexedEnum;
import lombok.Getter;

import java.util.List;

public enum TestServerEnum implements IndexedEnum {
    // 本地环境
    LOCAL_KCP(0, "127.0.0.1", 6666),
    LOCAL_TCP(1, "127.0.0.1", 8888),
    // 内网环境
    INNER_KCP(2, "1.1.1.1", 6666),
    INNER_TCP(3, "1.1.1.1", 8888),
    // 外网环境
    OUTER_KCP(4, "1.1.1.1", 40000),
    OUTER_TCP(5, "1.1.1.1", 40000),
    ;

    private int type;
    @Getter
    private String host;
    @Getter
    private int port;

    TestServerEnum(int type, String host, int port) {
        this.type = type;
        this.host = host;
        this.port = port;
    }

    @Override
    public int getIndex() {
        return this.type;
    }

    private static final List<TestServerEnum> values = IndexedEnumUtil.toIndexes(TestServerEnum.values());

    public static List<TestServerEnum> getValues() {
        return values;
    }

    public static TestServerEnum valueOf(int value) {
        return EnumUtil.valueOf(values, value);
    }
}