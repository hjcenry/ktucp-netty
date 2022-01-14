package com.hjcenry.net.client;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.ClientConvChannelManager;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KcpOutPutImp;
import com.hjcenry.kcp.KcpOutput;
import com.hjcenry.kcp.ScheduleTask;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.User;
import com.hjcenry.kcp.UserNetManager;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.net.AbstractNet;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.server.NetTypeEnum;
import com.hjcenry.net.callback.StartUpNettyServerCallBack;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 抽象网络客户端
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:02
 **/
public abstract class AbstractNetClient extends AbstractNet implements INetClient {

    /**
     * 启动器
     */
    protected Bootstrap bootstrap;

    public AbstractNetClient(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KcpInitException {
        super(netId, netTypeEnum, netConfigData);
    }

    @Override
    public void connect(Ukcp ukcp) throws KcpInitException {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();

        // 读取连接地址
        InetSocketAddress localAddress = netChannelConfig.getClientConnectLocalAddress();
        InetSocketAddress remoteAddress = netChannelConfig.getClientConnectRemoteAddress();
        if (localAddress == null) {
            localAddress = new InetSocketAddress(0);
        }

        StartUpNettyServerCallBack bindCallBack = netChannelConfig.getBindCallback();
        StartUpNettyServerCallBack activeCallBack = netChannelConfig.getActiveCallback();

        String clientClassName = this.getClass().getSimpleName();
        // 绑定端口回调
        bindCallBack = bindCallBack != null ? bindCallBack : new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future) {
                if (future == null || future.isSuccess()) {
                    if (logger.isInfoEnabled()) {
                        logger.info(clientClassName + " Net connect success local[{}] to remote[{}]: ",
                                netChannelConfig.getClientConnectLocalAddress(), netChannelConfig.getClientConnectRemoteAddress());
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(clientClassName + " Net connect failed local[{}] to remote[{}]: ",
                                netChannelConfig.getClientConnectLocalAddress(), netChannelConfig.getClientConnectRemoteAddress());
                    }
                }
            }
        };
        // 成功启动回调
        activeCallBack = activeCallBack != null ? activeCallBack : new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future) {
                if (future == null || future.isSuccess()) {
                    if (logger.isInfoEnabled()) {
                        logger.info(clientClassName + " Net active start success local[{}] to remote[{}]: ",
                                netChannelConfig.getClientConnectLocalAddress(), netChannelConfig.getClientConnectRemoteAddress());
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(clientClassName + " Net active failed at local[{}] to remote[{}]: ",
                                netChannelConfig.getClientConnectLocalAddress(), netChannelConfig.getClientConnectRemoteAddress());
                    }
                }
            }
        };
        // 需要等待两个callback都成功执行
        final CountDownLatch waiter = new CountDownLatch(2);
        // 连接
        this.connect(ukcp, localAddress, remoteAddress, waiter, bindCallBack, activeCallBack);
        try {
            // 等待服务成功启动。再继续后面的逻辑，1min超时
            if (!waiter.await(1, TimeUnit.MINUTES)) {
                logger.warn(String.format("%s start time out [%d%s]", this.getClass().getSimpleName(), 1, TimeUnit.MINUTES));
            }
        } catch (InterruptedException e) {
            // 抛出异常
            throw new KcpInitException(e);
        }
    }

    @Override
    public void reconnect(Ukcp ukcp) throws KcpInitException, UnsupportedOperationException {
        if (!(this.netConfigData.getChannelManager() instanceof ClientConvChannelManager)) {
            throw new UnsupportedOperationException("Reconnect can only be used in convChannel");
        }
        ukcp.getMessageExecutor().execute(() -> {
            User user = ukcp.user();

            UserNetManager userNetManager = user.getUserNetManager();
            Channel channel = userNetManager.getChannel(this.netId);
            channel.close();
            InetSocketAddress remoteAddress = userNetManager.getRemoteSocketAddress(this.netId);
            InetSocketAddress localAddress = new InetSocketAddress(0);

            ChannelFuture channelFuture = bootstrap.connect(remoteAddress, localAddress);
            this.onReconnected(ukcp, channelFuture, localAddress, remoteAddress);
        });
    }

    protected void onReconnected(Ukcp ukcp, ChannelFuture future, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        Channel channel = future.channel();
        // 修改网络
        ukcp.setCurrentNetId(this.netId);
        // 绑定通道
        this.bindChannel(ukcp, channel, localAddress, remoteAddress);
    }

    /**
     * 启动服务
     *
     * @param ukcp           KCP对象
     * @param localAddress   本地地址
     * @param remoteAddress  远端地址
     * @param waiter         等待
     * @param bindCallBack   绑定回调
     * @param activeCallBack 激活回调
     * @throws KcpInitException 初始异常
     */
    protected void connect(Ukcp ukcp, InetSocketAddress localAddress, InetSocketAddress remoteAddress, CountDownLatch waiter,
                           StartUpNettyServerCallBack bindCallBack, StartUpNettyServerCallBack activeCallBack) throws KcpInitException {
        // 初始线程组
        initGroups();
        // 初始并配置Netty启动类
        initConfigureBootStrap();
        // 配置连接参数
        applyConnectionOptions();

        ChannelFuture bindFuture = bootstrap.connect(remoteAddress, localAddress).addListener((FutureListener<Void>) future -> {
            // 绑定成功回调
            if (bindCallBack != null) {
                bindCallBack.apply(future);
            }
            // 计数
            waiter.countDown();
        });
        bindFuture.syncUninterruptibly().addListener((FutureListener<Void>) future -> {
            // 启动成功回调
            if (activeCallBack != null) {
                activeCallBack.apply(future);
            }
            this.onConnected(ukcp, (ChannelFuture) future, localAddress, remoteAddress);
            // 计数
            waiter.countDown();
        });
    }

    protected void onConnected(Ukcp ukcp, ChannelFuture future, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        Channel channel = future.channel();
        // 修改网络
        ukcp.setCurrentNetId(this.netId);
        // 绑定通道
        this.bindChannel(ukcp, channel, localAddress, remoteAddress);

        KcpListener kcpListener = this.netConfigData.getListener();

        // 消息处理器
        IMessageExecutorPool messageExecutorPool = this.netConfigData.getMessageExecutorPool();
        IMessageExecutor messageExecutor = messageExecutorPool.getMessageExecutor();

        // 切换网络
        ukcp.changeCurrentNetId(this.netId);

        messageExecutor.execute(() -> {
            try {
                // 所有网络共用一个连接事件
                kcpListener.onConnected(this.netId, ukcp);
            } catch (Throwable throwable) {
                kcpListener.handleException(throwable, ukcp);
            }
        });

        HashedWheelTimer hashedWheelTimer = this.netConfigData.getHashedWheelTimer();
        // 配置
        ChannelConfig channelConfig = this.netConfigData.getChannelConfig();
        boolean isKcpIdleTimeoutClose = channelConfig.isKcpIdleTimeoutClose();
        ScheduleTask scheduleTask = new ScheduleTask(messageExecutor, ukcp, hashedWheelTimer, isKcpIdleTimeoutClose);
        long delay = channelConfig.getInterval();
        // 启动客户端时间轮
        hashedWheelTimer.newTimeout(scheduleTask, delay, TimeUnit.MILLISECONDS);
    }

    protected void bindChannel(Ukcp ukcp, Channel channel, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        User user = ukcp.user();
        UserNetManager userNetManager = user.getUserNetManager();
        userNetManager.addNetInfo(this.netId, channel, localAddress, remoteAddress);
    }

    protected KcpOutput getKcpOutput() {
        return new KcpOutPutImp();
    }

    @Override
    public String toString() {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();
        return this.getClass().getSimpleName() + "{" +
                "connect= local:" + this.netConfigData.getNetChannelConfig().getClientConnectLocalAddress() +
                " -> remote:" + this.netConfigData.getNetChannelConfig().getClientConnectRemoteAddress() +
                ", ioGroup.num=" + netChannelConfig.getIoThreadNum() +
                '}';
    }
}
