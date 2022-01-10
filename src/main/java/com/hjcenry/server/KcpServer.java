package com.hjcenry.server;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KcpListener;
import com.hjcenry.kcp.ServerAddressChannelManager;
import com.hjcenry.kcp.ServerConvChannelManager;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.buffer.ByteBuf;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
public class KcpServer {

    private static final Logger LOG = LoggerFactory.getLogger(KcpServer.class);

    /**
     * 连接管理器
     */
    private IChannelManager channelManager;

    /**
     * 网络服务通道
     */
    List<INetServer> netServers = new ArrayList<>();

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
     * 初始化KCP网络服务
     *
     * @param kcpListener   KCP监听器
     * @param channelConfig 连接配置
     */
    public void init(KcpListener kcpListener, ChannelConfig channelConfig) {
        if (channelConfig.isUseConvChannel()) {
            int convIndex = 0;
            if (channelConfig.getFecAdapt() != null) {
                convIndex += Fec.fecHeaderSizePlus2;
            }
            // convId管理
            channelManager = new ServerConvChannelManager(convIndex);
        } else {
            // address管理
            channelManager = new ServerAddressChannelManager();
        }
        // 初始时间轮
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(new TimerThreadFactory(), 1, TimeUnit.MILLISECONDS);
        // 消息处理池
        IMessageExecutorPool iMessageExecutorPool = channelConfig.getIMessageExecutorPool();

        // 网络服务数据
        NetConfigData netConfigData = new NetConfigData();
        netConfigData.setChannelConfig(channelConfig);
        netConfigData.setiMessageExecutorPool(iMessageExecutorPool);
        netConfigData.setChannelManager(channelManager);
        netConfigData.setHashedWheelTimer(hashedWheelTimer);
        netConfigData.setListener(kcpListener);

        // 创建网络服务
        List<INetServer> netServers = new ArrayList<>();
        if (channelConfig.useTcp()) {
            // 创建TCP服务
            createNetServer(NetServerEnum.NET_TCP, netConfigData, netServers);
        }
        if (channelConfig.useUdp()) {
            // 创建UDP服务
            createNetServer(NetServerEnum.NET_UDP, netConfigData, netServers);
        }
        this.netServers = netServers;

        // 启动网络服务
        for (INetServer netServer : this.netServers) {
            try {
                netServer.start();
            } catch (KcpInitException e) {
                LOG.error(String.format("%s Start Failed", netServer.getClass().getSimpleName()));
            }
        }

        // 打印启动网络信息
        this.logPrintNetServer();

        // 停服钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void logPrintNetServer() {
        StringBuilder stringBuilder = new StringBuilder();
        for (INetServer netServer : this.netServers) {
            stringBuilder.append(netServer.toString()).append("\n");
        }
        LOG.info(String.format("%s Start : " +
                        "\n===========================================================\n" +
                        "%s" +
                        "===========================================================",
                this.getClass().getSimpleName(), stringBuilder));
    }

    private void createNetServer(NetServerEnum netServerEnum, NetConfigData netConfigData, List<INetServer> netServers) {
        INetServer tcpServer;
        try {
            tcpServer = NetServerFactory.createNetServer(netServerEnum, netConfigData);
            netServers.add(tcpServer);
        } catch (KcpInitException e) {
            LOG.error("", e);
        }
    }

    public void stop() {
        // 停止所有网络服务
        this.netServers.forEach(INetServer::stop);
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.getTcpChannelConfig().setBindPort(1111);
        channelConfig.getUdpChannelConfig().setBindPort(1111);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(new KcpListener() {
            @Override
            public void onConnected(Ukcp ukcp) {

            }

            @Override
            public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {

            }

            @Override
            public void handleException(Throwable ex, Ukcp ukcp) {

            }

            @Override
            public void handleClose(Ukcp ukcp) {

            }
        }, channelConfig);
    }
}
