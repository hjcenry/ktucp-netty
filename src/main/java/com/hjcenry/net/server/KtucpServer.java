package com.hjcenry.net.server;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KtucpNetManager;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.kcp.ServerAddressChannelManager;
import com.hjcenry.kcp.ServerConvChannelManager;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.net.INet;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.callback.IKtucpStartUpCallback;
import com.hjcenry.net.udp.UdpChannelConfig;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
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
public class KtucpServer {

    private static final Logger logger = KtucpLog.logger;

    /**
     * 连接管理器
     */
    private IChannelManager channelManager;
    private KtucpListener ktucpListener;
    private ChannelConfig channelConfig;
    private IMessageEncoder messageEncoder;
    private IMessageDecoder messageDecoder;
    private HashedWheelTimer hashedWheelTimer;
    private IMessageExecutorPool messageExecutorPool;

    /**
     * 网络服务ID
     */
    static AtomicInteger autoNetId = new AtomicInteger(0);

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
            Set<NetTypeEnum> netTypeEnums = new HashSet<>(channelConfig.getNetChannelConfigList().size());
            for (NetChannelConfig netChannelConfig : channelConfig.getNetChannelConfigList()) {
                netTypeEnums.add(netChannelConfig.getNetTypeEnum());
            }
            if (netTypeEnums.size() > 1) {
                // 超过一种类型的网络，不能使用Adress管理KCP
                if (logger.isErrorEnabled()) {
                    logger.error("Can not use address to manager channel when net size greater than 1");
                }
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
        for (INet net : KtucpNetManager.getAllNet()) {
            try {
                INetServer netServer = (INetServer) net;
                netServer.start();
            } catch (KtucpInitException e) {
                logger.error(String.format("%s Start Failed", net.getClass().getSimpleName()));
            }
        }

        // 打印启动网络信息
        this.logPrintNetServer();

        // 启动完成回调
        IKtucpStartUpCallback callback = channelConfig.getStartUpCallback();
        if (callback != null) {
            callback.apply();
        }

        // 停服钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    protected void createNetServers() {
        for (NetChannelConfig netChannelConfig : channelConfig.getNetChannelConfigList()) {
            int autoId = autoNetId.incrementAndGet();
            int netId = netChannelConfig.getNetId();
            // 配置了取自定义id，否则取自增id
            netId = netId > 0 ? netId : autoId;
            // 判重
            if (KtucpNetManager.containsNet(netId)) {
                // ID重复
                if (logger.isErrorEnabled()) {
                    logger.error(String.format("create net failed : netId[%d] exist", netId));
                }
                continue;
            }
            // 网络服务数据
            NetConfigData netConfigData = new NetConfigData();
            // 配置数据
            netConfigData.setChannelConfig(channelConfig);
            netConfigData.setNetChannelConfig(netChannelConfig);
            // 处理器
            netConfigData.setMessageExecutorPool(this.messageExecutorPool);
            netConfigData.setChannelManager(channelManager);
            netConfigData.setHashedWheelTimer(hashedWheelTimer);
            // 监听和解码
            netConfigData.setListener(ktucpListener);
            netConfigData.setMessageEncoder(messageEncoder);
            netConfigData.setMessageDecoder(messageDecoder);
            // 创建网络服务
            INet netServer = createNetServer(netId, netChannelConfig.getNetTypeEnum(), netConfigData);
            if (netServer == null) {
                continue;
            }
            // 添加到网络manager
            KtucpNetManager.addNet(netServer);
        }
    }

    private void logPrintNetServer() {
        StringBuilder stringBuilder = new StringBuilder();
        for (INet netServer : KtucpNetManager.getAllNet()) {
            stringBuilder.append(netServer.toString()).append("\n");
        }
        logger.info(String.format("%s Start : " +
                        "\n===========================================================\n" +
                        "%s" +
                        "===========================================================",
                this.getClass().getSimpleName(), stringBuilder));
    }

    private INet createNetServer(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) {
        try {
            return NetServerFactory.createNetServer(netId, netTypeEnum, netConfigData);
        } catch (KtucpInitException e) {
            logger.error("", e);
            return null;
        }
    }

    public void stop() {
        // 停止所有网络服务
        KtucpNetManager.getAllNet().forEach(INet::stop);
        if (this.messageExecutorPool != null) {
            this.messageExecutorPool.stop();
        }
        if (this.hashedWheelTimer != null) {
            this.hashedWheelTimer.stop();
        }
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }
}
