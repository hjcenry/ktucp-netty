package com.hjcenry.server;

import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KcpListener;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.util.HashedWheelTimer;

/**
 * 网络服务数据
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 16:38
 **/
public class NetConfigData {

    /**
     * 网络配置
     */
    private ChannelConfig channelConfig;
    /**
     * 处理器
     */
    private IMessageExecutorPool iMessageExecutorPool;
    /**
     * 连接管理器
     */
    private IChannelManager channelManager;
    /**
     * 调度时间轮
     */
    private HashedWheelTimer hashedWheelTimer;
    /**
     * 监听器
     */
    private KcpListener listener;

    public ChannelConfig getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(ChannelConfig channelConfig) {
        this.channelConfig = channelConfig;
    }

    public IMessageExecutorPool getiMessageExecutorPool() {
        return iMessageExecutorPool;
    }

    public void setiMessageExecutorPool(IMessageExecutorPool iMessageExecutorPool) {
        this.iMessageExecutorPool = iMessageExecutorPool;
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }

    public void setChannelManager(IChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public HashedWheelTimer getHashedWheelTimer() {
        return hashedWheelTimer;
    }

    public void setHashedWheelTimer(HashedWheelTimer hashedWheelTimer) {
        this.hashedWheelTimer = hashedWheelTimer;
    }

    public KcpListener getListener() {
        return listener;
    }

    public void setListener(KcpListener listener) {
        this.listener = listener;
    }
}
