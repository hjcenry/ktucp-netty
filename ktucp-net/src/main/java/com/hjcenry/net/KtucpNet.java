package com.hjcenry.net;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.kcp.*;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.threadpool.IMessageExecutorPool;
import com.hjcenry.util.Assert;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/16 14:32
 **/
public abstract class KtucpNet {

    protected static final Logger logger = KtucpLog.logger;
    /**
     * channel管理
     */
    protected IChannelManager channelManager;
    /**
     * 监听器
     */
    protected KtucpListener ktucpListener;
    /**
     * 配置
     */
    protected ChannelConfig channelConfig;
    /**
     * 编码器
     */
    protected IMessageEncoder messageEncoder;
    /**
     * 解码器
     */
    protected IMessageDecoder messageDecoder;
    /**
     * 时间轮
     */
    protected HashedWheelTimer hashedWheelTimer;
    /**
     * 线程池
     */
    protected IMessageExecutorPool messageExecutorPool;
    /**
     * 网络管理器
     */
    protected final KtucpNetManager ktucpNetManager = new KtucpNetManager();

    /**
     * 检测是否可通过地址管理网络
     *
     * @param channelConfig 配置
     * @return 是否可通过地址管理网络
     */
    protected boolean checkCanManageAddress(ChannelConfig channelConfig) {
        Set<NetTypeEnum> netTypeEnums = new HashSet<>(channelConfig.getNetChannelConfigList().size());
        for (INetChannelConfig netChannelConfig : channelConfig.getNetChannelConfigList()) {
            NetChannelConfig config = (NetChannelConfig) netChannelConfig;
            netTypeEnums.add(config.getNetTypeEnum());
        }
        if (netTypeEnums.size() > 1) {
            // 超过一种类型的网络，不能使用Address管理KCP
            if (logger.isErrorEnabled()) {
                logger.error("Can not use address to manager channel when net size greater than 1");
            }
            return false;
        }
        return true;
    }

    /**
     * 获取网络配置
     *
     * @param config 网络配置
     * @return 网络配置
     */
    protected NetConfigData getNetConfigData(NetChannelConfig config) {
        // 网络服务数据
        NetConfigData netConfigData = new NetConfigData();
        // 配置数据
        netConfigData.setChannelConfig(channelConfig);
        netConfigData.setNetChannelConfig(config);
        // 处理器
        netConfigData.setMessageExecutorPool(messageExecutorPool);
        netConfigData.setChannelManager(channelManager);
        netConfigData.setHashedWheelTimer(hashedWheelTimer);
        // 监听和解码
        netConfigData.setListener(ktucpListener);
        netConfigData.setMessageEncoder(messageEncoder);
        netConfigData.setMessageDecoder(messageDecoder);
        return netConfigData;
    }

    /**
     * 获取网络id
     *
     * @param config 网络配置
     * @return 网络id
     */
    protected int getNetId(NetChannelConfig config) {
        int netId = config.getNetId();
        if (netId > 0) {
            return netId;
        }
        // 配置了取自定义id，否则取自增id
        return KtucpGlobalNetManager.createNetId();
    }

    protected void logPrintNet() {
        StringBuilder stringBuilder = new StringBuilder();
        for (INet net : this.ktucpNetManager.getAllNet()) {
            stringBuilder.append(net.toString()).append("\n");
        }
        logger.info(String.format("%s Connect : " +
                        "\n===========================================================\n" +
                        "%s" +
                        "===========================================================",
                this.getClass().getSimpleName(), stringBuilder));
    }

    public void stop() {
        // 停止所有网络
        for (INet net : this.ktucpNetManager.getAllNet()) {
            net.stop();
        }
        this.ktucpNetManager.clear();
        if (this.messageExecutorPool != null) {
            this.messageExecutorPool.stop();
        }
        if (this.hashedWheelTimer != null) {
            this.hashedWheelTimer.stop();
        }
    }
}
