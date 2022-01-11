package com.hjcenry.server;

import com.hjcenry.coder.IMessageDecoder;
import com.hjcenry.coder.IMessageEncoder;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.listener.KcpListener;
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
    /**
     * 消息编码
     */
    private IMessageEncoder messageEncoder;
    /**
     * 消息解码
     */
    private IMessageDecoder messageDecoder;

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

    public IMessageEncoder getMessageEncoder() {
        return messageEncoder;
    }

    public void setMessageEncoder(IMessageEncoder messageEncoder) {
        this.messageEncoder = messageEncoder;
    }

    public IMessageDecoder getMessageDecoder() {
        return messageDecoder;
    }

    public void setMessageDecoder(IMessageDecoder messageDecoder) {
        this.messageDecoder = messageDecoder;
    }
}
