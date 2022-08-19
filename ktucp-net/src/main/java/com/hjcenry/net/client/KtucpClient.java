package com.hjcenry.net.client;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.*;
import com.hjcenry.kcp.NetTypeEnum;
import com.hjcenry.net.KtucpNet;
import com.hjcenry.threadpool.IMessageExecutor;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.udp.UdpChannelConfig;
import com.hjcenry.util.Assert;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
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
public class KtucpClient extends KtucpNet {

    private static final Logger logger = KtucpLog.logger;

    /**
     * KCP对象
     */
    private Uktucp uktucp;
    /**
     * 定时器
     */
    private ScheduleTask scheduleTask;

    /**
     * 定时器线程工厂
     **/
    private static class TimerThreadFactory implements ThreadFactory {
        private final AtomicInteger timeThreadName = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "KcpClientTimerThread " + timeThreadName.addAndGet(1));
        }
    }

    /**
     * 默认初始化KCP网络
     * <p>该方法只启动一个UDP</p>
     *
     * @param ktucpListener KCP监听器
     * @param channelConfig 连接配置
     * @param remoteAddress 远端地址
     */
    public void init(KtucpListener ktucpListener, ChannelConfig channelConfig, InetSocketAddress remoteAddress) {
        channelConfig.addNetChannelConfig(UdpChannelConfig.buildClientConfig(remoteAddress));
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
            this.channelManager = new ClientConvChannelManager(convIndex);
        } else {
            // address管理
            if (!checkCanManageAddress(channelConfig)) {
                return;
            }
            this.channelManager = new ClientAddressChannelManager();
        }

        this.channelConfig = channelConfig;
        this.ktucpListener = ktucpListener;
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;

        // 初始时间轮
        this.hashedWheelTimer = new HashedWheelTimer(new TimerThreadFactory(), 1, TimeUnit.MILLISECONDS);
        // 消息处理池
        this.messageExecutorPool = channelConfig.getMessageExecutorPool();
        IMessageExecutor messageExecutor = messageExecutorPool.getMessageExecutor();

        // 创建KCP对象
        this.uktucp = this.createUkcp(messageExecutor);

        // 创建网络服务
        createNetClients();

        // 配置
        long delay = channelConfig.getInterval();
        // 启动客户端时间轮
        hashedWheelTimer.newTimeout(this.scheduleTask, delay, TimeUnit.MILLISECONDS);

        // 停服钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * 开始连接
     */
    public void connect() {
        // 启动网络服务
        for (INet net : this.ktucpNetManager.getAllNet()) {
            try {
                INetClient client = (INetClient) net;
                client.connect(this.uktucp);
            } catch (KtucpInitException e) {
                logger.error(String.format("%s Start Failed", net.getClass().getSimpleName()));
            }
        }

        // 打印启动网络信息
        this.logPrintNet();

        // 启动完成回调
        IKtucpClientStartUpCallback callback = channelConfig.getClientStartUpCallback();
        if (callback != null) {
            callback.apply(this.uktucp);
        }
    }

    /**
     * 重连接口
     * 使用旧的kcp对象，出口ip和端口替换为新的
     * 在4G切换为wifi等场景使用
     */
    public void reconnect() {
        // 启动网络服务
        for (INet net : this.ktucpNetManager.getAllNet()) {
            try {
                INetClient client = (INetClient) net;
                client.reconnect(this.uktucp);
            } catch (KtucpInitException e) {
                logger.error(String.format("%s Start Failed", net.getClass().getSimpleName()));
            }
        }
    }

    public void reconnect(int netId) {
        // 启动网络服务
        INet net = KtucpGlobalNetManager.getNet(netId);
        if (net == null) {
            return;
        }
        try {
            INetClient client = (INetClient) net;
            client.reconnect(this.uktucp);
        } catch (KtucpInitException e) {
            logger.error(String.format("%s Reconnect Failed", net.getClass().getSimpleName()));
        }
    }

    /**
     * 创建KCP对象
     * <p>客户端的KCP对象在一个KcpClient对象中创建</p>
     *
     * @param messageExecutor 消息处理器
     * @return KCP对象
     */
    protected Uktucp createUkcp(IMessageExecutor messageExecutor) {
        // KCP输出接口
        KtucpOutput ktucpOutput = new KtucpOutPutImp();
        // 创建KCP对象
        Uktucp newUktucp = new Uktucp(ktucpOutput, ktucpListener, messageExecutor, channelConfig, channelManager, messageEncoder, messageDecoder);
        this.scheduleTask = new ScheduleTask(messageExecutor, newUktucp, hashedWheelTimer, channelConfig.isKcpIdleTimeoutClose());
        // 创建user
        User user = new User(channelConfig.getNetNum());
        newUktucp.user(user);
        // 客户端模式
        newUktucp.setClientMode();
        // 设置convId
        int conv = channelConfig.getConv();
        newUktucp.setConv(conv);
        // 添加ukcp管理
        channelManager.addKcp(newUktucp);
        return newUktucp;
    }

    protected void createNetClients() {
        for (INetChannelConfig netChannelConfig : channelConfig.getNetChannelConfigList()) {
            Assert.instanceOf(netChannelConfig, NetChannelConfig.class, "INetChannelConfig is not NetChannelConfig");
            NetChannelConfig config = (NetChannelConfig) netChannelConfig;
            // 网络id
            int netId = getNetId(config);
            // 网络服务数据
            NetConfigData netConfigData = getNetConfigData(config);
            // 创建网络服务
            INet netClient = createNetClient(netId, config.getNetTypeEnum(), netConfigData);
            if (netClient == null) {
                continue;
            }
            // 添加到网络manager
            this.ktucpNetManager.addNet(netClient);
            KtucpGlobalNetManager.addNet(netClient);
        }
    }

    private INet createNetClient(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) {
        try {
            return NetClientFactory.createNetClient(netId, netTypeEnum, netConfigData);
        } catch (KtucpInitException e) {
            logger.error("", e);
            return null;
        }
    }

    public Uktucp getUkcp() {
        return uktucp;
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }
}
