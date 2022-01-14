package com.hjcenry.net.client;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.ClientAddressChannelManager;
import com.hjcenry.kcp.ClientConvChannelManager;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KtucpNetManager;
import com.hjcenry.kcp.KtucpOutPutImp;
import com.hjcenry.kcp.KtucpOutput;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.User;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.net.INet;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.server.NetTypeEnum;
import com.hjcenry.net.udp.UdpChannelConfig;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
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
public class KtucpClient {

    private static final Logger logger = KtucpLog.logger;

    /**
     * 线程池
     */
    private IMessageExecutorPool messageExecutorPool;
    /**
     * 时间轮
     */
    private HashedWheelTimer hashedWheelTimer;
    /**
     * 连接管理器
     */
    private IChannelManager channelManager;
    /**
     * KCP对象
     */
    private Uktucp uktucp;
    /**
     * 配置
     */
    private ChannelConfig channelConfig;
    /**
     * 监听器
     */
    private KtucpListener ktucpListener;
    /**
     * 编码器
     */
    private IMessageEncoder messageEncoder;
    /**
     * 解码器
     */
    private IMessageDecoder messageDecoder;
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
            return new Thread(r, "KcpClientTimerThread " + timeThreadName.addAndGet(1));
        }
    }

    /**
     * 默认初始化KCP网络
     * <p>该方法只启动一个UDP</p>
     *
     * @param ktucpListener   KCP监听器
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
     * @param ktucpListener   KCP监听器
     * @param channelConfig 连接配置
     */
    public void init(KtucpListener ktucpListener, ChannelConfig channelConfig) {
        // 使用默认编解码（传原始ByteBuf）
        this.init(ktucpListener, channelConfig, null, null);
    }

    /**
     * 初始化KCP网络服务
     *
     * @param ktucpListener    KCP监听器
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

        // 停服钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * 开始连接
     */
    public void connect() {
        // 启动网络服务
        for (INet net : KtucpNetManager.getAllNet()) {
            try {
                INetClient client = (INetClient) net;
                client.connect(this.uktucp);
            } catch (KtucpInitException e) {
                logger.error(String.format("%s Start Failed", net.getClass().getSimpleName()));
            }
        }

        // 打印启动网络信息
        this.logPrintNetServer();
    }

    /**
     * 重连接口
     * 使用旧的kcp对象，出口ip和端口替换为新的
     * 在4G切换为wifi等场景使用
     */
    public void reconnect() {
        // 启动网络服务
        for (INet net : KtucpNetManager.getAllNet()) {
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
        INet net = KtucpNetManager.getNet(netId);
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
        // 创建user
        User user = new User(channelConfig.getNetNum());
        newUktucp.user(user);
        // 客户端模式
        newUktucp.setClientMode();

        // 添加ukcp管理
        channelManager.addKcp(newUktucp);
        return newUktucp;
    }

    protected void createNetClients() {
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
            netConfigData.setMessageExecutorPool(messageExecutorPool);
            netConfigData.setChannelManager(channelManager);
            netConfigData.setHashedWheelTimer(hashedWheelTimer);
            // 监听和解码
            netConfigData.setListener(ktucpListener);
            netConfigData.setMessageEncoder(messageEncoder);
            netConfigData.setMessageDecoder(messageDecoder);
            // 创建网络服务
            INet netClient = createNetClient(netId, netChannelConfig.getNetTypeEnum(), netConfigData);
            if (netClient == null) {
                continue;
            }
            // 添加到网络manager
            KtucpNetManager.addNet(netClient);
        }
    }

    private void logPrintNetServer() {
        StringBuilder stringBuilder = new StringBuilder();
        for (INet netServer : KtucpNetManager.getAllNet()) {
            stringBuilder.append(netServer.toString()).append("\n");
        }
        logger.info(String.format("%s Connect : " +
                        "\n===========================================================\n" +
                        "%s" +
                        "===========================================================",
                this.getClass().getSimpleName(), stringBuilder));
    }

    private INet createNetClient(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) {
        try {
            return NetClientFactory.createNetClient(netId, netTypeEnum, netConfigData);
        } catch (KtucpInitException e) {
            logger.error("", e);
            return null;
        }
    }

    public void stop() {
        // 停止所有网络
        KtucpNetManager.getAllNet().forEach(INet::stop);
        if (this.messageExecutorPool != null) {
            this.messageExecutorPool.stop();
        }
        if (this.hashedWheelTimer != null) {
            this.hashedWheelTimer.stop();
        }
    }

    public Uktucp getUkcp() {
        return uktucp;
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }
}
