package com.hjcenry.net.client;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.ClientConvChannelManager;
import com.hjcenry.kcp.KtucpOutPutImp;
import com.hjcenry.kcp.KtucpOutput;
import com.hjcenry.kcp.ScheduleTask;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.User;
import com.hjcenry.kcp.UserNetManager;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.log.KtucpLog;
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

    public AbstractNetClient(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        super(netId, netTypeEnum, netConfigData);
    }

    @Override
    protected void checkConfigData() throws KtucpInitException {
        super.checkConfigData();
        if (this.netConfigData.getChannelConfig().isUseConvChannel() && this.netConfigData.getChannelConfig().getConv() == 0) {
            // 使用convId管理连接，convId传0给个warn吧
            if (KtucpLog.logger.isWarnEnabled()) {
                KtucpLog.logger.warn(String.format("net[%d] use conv channel with convId is 0", this.netId));
            }
        }
    }

    @Override
    public void connect(Uktucp uktucp) throws KtucpInitException {
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
            public void apply(Future<Void> future, int netId) {
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
            public void apply(Future<Void> future, int netId) {
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
        this.connect(uktucp, localAddress, remoteAddress, waiter, bindCallBack, activeCallBack);
        try {
            // 等待服务成功启动。再继续后面的逻辑，1min超时
            if (!waiter.await(1, TimeUnit.MINUTES)) {
                logger.warn(String.format("%s start time out [%d%s]", this.getClass().getSimpleName(), 1, TimeUnit.MINUTES));
            }
        } catch (InterruptedException e) {
            // 抛出异常
            throw new KtucpInitException(e);
        }
    }

    @Override
    public void reconnect(Uktucp uktucp) throws KtucpInitException, UnsupportedOperationException {
        if (!(this.netConfigData.getChannelManager() instanceof ClientConvChannelManager)) {
            throw new UnsupportedOperationException("Reconnect can only be used in convChannel");
        }
        uktucp.getMessageExecutor().execute(() -> {
            User user = uktucp.user();

            UserNetManager userNetManager = user.getUserNetManager();
            Channel channel = userNetManager.getChannel(this.netId);
            channel.close();
            InetSocketAddress remoteAddress = userNetManager.getRemoteSocketAddress(this.netId);
            InetSocketAddress localAddress = new InetSocketAddress(0);

            ChannelFuture channelFuture = bootstrap.connect(remoteAddress, localAddress);
            this.onReconnected(uktucp, channelFuture, localAddress, remoteAddress);
        });
    }

    protected void onReconnected(Uktucp uktucp, ChannelFuture future, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        Channel channel = future.channel();
        // 修改网络
        uktucp.setCurrentNetId(this.netId);
        // 绑定通道
        this.bindChannel(uktucp, channel, localAddress, remoteAddress);
    }

    /**
     * 启动服务
     *
     * @param uktucp         KCP对象
     * @param localAddress   本地地址
     * @param remoteAddress  远端地址
     * @param waiter         等待
     * @param bindCallBack   绑定回调
     * @param activeCallBack 激活回调
     * @throws KtucpInitException 初始异常
     */
    protected void connect(Uktucp uktucp, InetSocketAddress localAddress, InetSocketAddress remoteAddress, CountDownLatch waiter,
                           StartUpNettyServerCallBack bindCallBack, StartUpNettyServerCallBack activeCallBack) throws KtucpInitException {
        // 初始线程组
        initGroups();
        // 初始并配置Netty启动类
        initConfigureBootStrap();
        // 配置连接参数
        applyConnectionOptions();

        ChannelFuture bindFuture = bootstrap.connect(remoteAddress, localAddress).addListener((FutureListener<Void>) future -> {
            // 绑定成功回调
            if (bindCallBack != null) {
                bindCallBack.apply(future, this.netId);
            }
            // 计数
            waiter.countDown();
        });
        bindFuture.syncUninterruptibly().addListener((FutureListener<Void>) future -> {
            // 启动成功回调
            if (activeCallBack != null) {
                activeCallBack.apply(future, this.netId);
            }
            this.onConnected(uktucp, (ChannelFuture) future, localAddress, remoteAddress);
            // 计数
            waiter.countDown();
        });
    }

    protected void onConnected(Uktucp uktucp, ChannelFuture future, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        Channel channel = future.channel();
        // 修改网络
        uktucp.setCurrentNetId(this.netId);
        // 绑定通道
        this.bindChannel(uktucp, channel, localAddress, remoteAddress);

        KtucpListener ktucpListener = this.netConfigData.getListener();

        // 消息处理器
        IMessageExecutorPool messageExecutorPool = this.netConfigData.getMessageExecutorPool();
        IMessageExecutor messageExecutor = messageExecutorPool.getMessageExecutor();

        // 切换网络
        uktucp.changeCurrentNetId(this.netId);

        messageExecutor.execute(() -> {
            try {
                // 所有网络共用一个连接事件
                ktucpListener.onConnected(this.netId, uktucp);
            } catch (Throwable throwable) {
                ktucpListener.handleException(throwable, uktucp);
            }
        });
    }

    protected void bindChannel(Uktucp uktucp, Channel channel, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        User user = uktucp.user();
        UserNetManager userNetManager = user.getUserNetManager();
        userNetManager.addNetInfo(this.netId, channel, localAddress, remoteAddress);
    }

    protected KtucpOutput getKcpOutput() {
        return new KtucpOutPutImp();
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
