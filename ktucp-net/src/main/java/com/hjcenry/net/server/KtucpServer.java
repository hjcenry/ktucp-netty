package com.hjcenry.net.server;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.*;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.KtucpNet;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.udp.UdpChannelConfig;
import com.hjcenry.util.Assert;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KCP服务
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 11:56
 **/
public class KtucpServer extends KtucpNet {

    /**
     * 定时器线程工厂
     **/
    private static class TimerThreadFactory implements ThreadFactory {
        private final AtomicInteger timeThreadName = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "KcpServerTimerThread " + timeThreadName.addAndGet(1));
        }
    }

    /**
     * 默认初始化KCP网络服务
     * <p>该方法只启动一个UDP服务</p>
     *
     * @param ktucpListener KCP监听器
     * @param channelConfig 连接配置
     * @param bindPort      绑定端口
     */
    public void init(KtucpListener ktucpListener, ChannelConfig channelConfig, int bindPort) {
        channelConfig.addNetChannelConfig(UdpChannelConfig.buildServerConfig(bindPort));
        // 使用默认编解码（传原始ByteBuf）
        this.init(ktucpListener, channelConfig, null, null);
    }

    /**
     * 初始化KCP网络服务
     *
     * @param ktucpListener KCP监听器
     * @param channelConfig 连接配置
     */
    public void init(KtucpListener ktucpListener, ChannelConfig channelConfig) {
        // 使用默认编解码（传原始ByteBuf）
        this.init(ktucpListener, channelConfig, null, null);
    }

    /**
     * 初始化KCP网络服务
     *
     * @param ktucpListener  KCP监听器
     * @param channelConfig  连接配置
     * @param messageDecoder 解码器
     * @param messageEncoder 编码器
     */
    public void init(KtucpListener ktucpListener, ChannelConfig channelConfig, IMessageEncoder messageEncoder, IMessageDecoder messageDecoder) {
        if (channelConfig.isUseConvChannel()) {
            int convIndex = 0;
            if (channelConfig.getFecAdapt() != null) {
                convIndex += Fec.fecHeaderSizePlus2;
            }
            // convId管理
            this.channelManager = new ServerConvChannelManager(convIndex);
        } else {
            // address管理
            if (!checkCanManageAddress(channelConfig)) {
                return;
            }
            this.channelManager = new ServerAddressChannelManager();
        }

        this.ktucpListener = ktucpListener;
        this.channelConfig = channelConfig;
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;

        // 初始时间轮
        this.hashedWheelTimer = new HashedWheelTimer(new TimerThreadFactory(), 1, TimeUnit.MILLISECONDS);
        // 消息处理池
        this.messageExecutorPool = channelConfig.getMessageExecutorPool();

        // 创建网络服务
        createNetServers();

        // 启动网络服务
        for (INet net : this.ktucpNetManager.getAllNet()) {
            try {
                INetServer netServer = (INetServer) net;
                netServer.start();
            } catch (KtucpInitException e) {
                logger.error(String.format("%s Start Failed", net.getClass().getSimpleName()));
            }
        }

        // 打印启动网络信息
        this.logPrintNet();

        // 启动完成回调
        IKtucpServerStartUpCallback callback = channelConfig.getServerStartUpCallback();
        if (callback != null) {
            callback.apply();
        }

        // 停服钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    protected void createNetServers() {
        for (INetChannelConfig netChannelConfig : channelConfig.getNetChannelConfigList()) {
            Assert.instanceOf(netChannelConfig, NetChannelConfig.class, "INetChannelConfig is not NetChannelConfig");
            NetChannelConfig config = (NetChannelConfig) netChannelConfig;
            // 网络id
            int netId = getNetId(config);
            // 判断id重复
            Assert.isTrue(!this.ktucpNetManager.containsNet(netId), String.format("create net failed : netId[%d] exist", netId));
            // 网络服务数据
            NetConfigData netConfigData = getNetConfigData(config);
            // 创建网络服务
            INet netServer = createNetServer(netId, config.getNetTypeEnum(), netConfigData);
            if (netServer == null) {
                continue;
            }
            // 添加到网络manager
            this.ktucpNetManager.addNet(netServer);
        }
    }

    private INet createNetServer(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) {
        try {
            return NetServerFactory.createNetServer(netId, netTypeEnum, netConfigData);
        } catch (KtucpInitException e) {
            logger.error("", e);
            return null;
        }
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }
}
