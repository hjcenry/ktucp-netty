package com.hjcenry.kcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * kcp网络管理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 10:11
 **/
public class KtucpGlobalNetManager {

    /**
     * 网络ID
     */
    private final static AtomicInteger AUTO_NET_ID = new AtomicInteger(1);
    /**
     * 网络map
     */
    private static final Map<Integer, INet> NET_MAP = new ConcurrentHashMap<>();

    /**
     * 网络ID计数器
     */
    private static final IntUnaryOperator NET_ID_INCREMENT_COUNTER = operand -> {
        if (operand >= Integer.MAX_VALUE - 1) {
            // 达到Int最大值，从1开始重新计数
            operand = 0;
        }
        operand++;
        return operand;
    };

    /**
     * 创建网络id
     *
     * @return 网络id
     */
    public static int createNetId() {
        return AUTO_NET_ID.getAndUpdate(NET_ID_INCREMENT_COUNTER);
    }

    /**
     * 添加网络
     *
     * @param net 网络
     */
    public static void addNet(INet net) {
        NET_MAP.put(net.getNetId(), net);
    }

    /**
     * 移除网络
     *
     * @param net 网络
     */
    public static void removeNet(INet net) {
        NET_MAP.remove(net.getNetId());
    }

    /**
     * 获取网络
     *
     * @param netId 网络id
     * @return 网络
     */
    public static INet getNet(int netId) {
        if (!NET_MAP.containsKey(netId)) {
            return null;
        }
        return NET_MAP.get(netId);
    }

    /**
     * 网络是否存在
     *
     * @param netId 网络id
     * @return 网络是否存在
     */
    public static boolean containsNet(int netId) {
        return NET_MAP.containsKey(netId);
    }
}
