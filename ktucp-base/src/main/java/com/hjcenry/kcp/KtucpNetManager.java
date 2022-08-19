package com.hjcenry.kcp;

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
public class KtucpNetManager {

    /**
     * 网络map
     */
    private final Map<Integer, INet> NET_MAP = new HashMap<>();

    /**
     * 添加网络
     *
     * @param net 网络
     */
    public void addNet(INet net) {
        NET_MAP.put(net.getNetId(), net);
    }

    /**
     * 移除网络
     *
     * @param net 网络
     */
    public void removeNet(INet net) {
        NET_MAP.remove(net.getNetId());
    }

    /**
     * 获取网络
     *
     * @param netId 网络id
     * @return 网络
     */
    public INet getNet(int netId) {
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
    public boolean containsNet(int netId) {
        return NET_MAP.containsKey(netId);
    }

    /**
     * 获取所有网络
     *
     * @return 所有网络
     */
    public Collection<INet> getAllNet() {
        return NET_MAP.values();
    }

    /**
     * 清空网络
     */
    public void clear() {
        NET_MAP.clear();
    }
}
