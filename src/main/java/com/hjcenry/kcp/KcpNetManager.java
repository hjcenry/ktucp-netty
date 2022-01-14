package com.hjcenry.kcp;

import com.hjcenry.net.INet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * kcp网络管理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 10:11
 **/
public class KcpNetManager {

    /**
     * 网络map
     */
    private static final Map<Integer, INet> NET_MAP = new HashMap<>();

    /**
     * 添加网络
     *
     * @param net 网络
     */
    public static void addNet(INet net) {
        NET_MAP.put(net.getNetId(), net);
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

    /**
     * 获取所有网络
     *
     * @return 所有网络
     */
    public static Collection<INet> getAllNet() {
        return NET_MAP.values();
    }
}
