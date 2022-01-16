package com.hjcenry.net;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KtucpNetManager;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.threadpool.IMessageExecutorPool;
import com.hjcenry.util.Assert;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

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
     * 网络ID
     */
    protected static AtomicInteger autoNetId = new AtomicInteger(0);

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
        int autoId = autoNetId.incrementAndGet();
        // 配置了取自定义id，否则取自增id
        netId = netId > 0 ? netId : autoId;
        // 判重
        Assert.isTrue(!KtucpNetManager.containsNet(netId), String.format("create net failed : netId[%d] exist", netId));
        return netId;
    }
}
