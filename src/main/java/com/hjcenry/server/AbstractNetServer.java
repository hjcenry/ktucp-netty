package com.hjcenry.server;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.server.callback.StartUpNettyServerCallBack;
import com.hjcenry.system.SystemOS;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 抽象网络服务
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:02
 **/
public abstract class AbstractNetServer implements INetServer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNetServer.class);

    protected int netId;

    protected AbstractBootstrap<?, ?> bootstrap;
    /**
     * BOSS线程组
     */
    protected EventLoopGroup bossGroup;
    /**
     * IO线程组
     */
    protected EventLoopGroup ioGroup;
    /**
     * 服务端channel集合
     */
    protected final List<Channel> serverChannels = new Vector<>();
    /**
     * 网络配置数据
     */
    protected NetConfigData netConfigData;
    /**
     * Netty网络通道类型
     */
    protected NettyGroupChannel nettyGroupChannel = NettyGroupChannel.getNettyGroupChannel();

    public AbstractNetServer(int netId, NetConfigData netConfigData) throws KcpInitException {
        this.netId = netId;
        this.netConfigData = netConfigData;
        this.checkConfigData();
    }

    /**
     * 检测网络服务参数合法性
     */
    protected void checkConfigData() throws KcpInitException {
        if (this.netConfigData == null) {
            throw new KcpInitException("NetConfigData null");
        }
        if (this.netConfigData.getChannelConfig() == null) {
            throw new KcpInitException("NetServer Channel Config can not be null");
        }
        if (this.netConfigData.getNetChannelConfig() == null) {
            throw new KcpInitException("NetServer Net Channel Config can not be null");
        }
        if (this.netConfigData.getListener() == null) {
            throw new KcpInitException("NetServer Listener can not be null");
        }
        if (this.netConfigData.getChannelManager() == null) {
            throw new KcpInitException("NetServer ChannelManager can not be null");
        }
        if (this.netConfigData.getHashedWheelTimer() == null) {
            throw new KcpInitException("NetServer HashedWheelTimer can not be null");
        }
        if (this.netConfigData.getMessageExecutorPool() == null) {
            throw new KcpInitException("NetServer MessageExecutorPool can not be null");
        }
    }

    @Override
    public void stop() {
        IChannelManager channelManager = netConfigData.getChannelManager();
        channelManager.getAll().forEach(Ukcp::close);
        if (netConfigData.getMessageExecutorPool() != null) {
            netConfigData.getMessageExecutorPool().stop();
        }
        if (netConfigData.getHashedWheelTimer() != null) {
            netConfigData.getHashedWheelTimer().stop();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (ioGroup != null) {
            ioGroup.shutdownGracefully();
        }
    }

    @Override
    public void start() throws KcpInitException {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();

        int bindPort = netChannelConfig.getBindPort();
        if (bindPort < 0) {
            throw new KcpInitException(String.format("%s start failed , port is [%d]", this.getClass().getSimpleName(), bindPort));
        }

        StartUpNettyServerCallBack bindCallBack = netChannelConfig.getBindCallback();
        StartUpNettyServerCallBack activeCallBack = netChannelConfig.getActiveCallback();

        String serverClassName = this.getClass().getSimpleName();
        // 绑定端口回调
        bindCallBack = bindCallBack != null ? bindCallBack : new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future) {
                if (future == null || future.isSuccess()) {
                    LOG.info(serverClassName + " Net bind start success at port: " + bindPort);
                } else {
                    LOG.info(serverClassName + " Net bind failed at port: " + bindPort);
                }
            }
        };
        // 成功启动回调
        activeCallBack = activeCallBack != null ? activeCallBack : new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future) {
                if (future == null || future.isSuccess()) {
                    LOG.info(serverClassName + " Net active start success at port: " + bindPort);
                } else {
                    LOG.info(serverClassName + " Net active failed at port: " + bindPort);
                }
            }
        };
        // 需要等待两个callback都成功执行
        final CountDownLatch waiter = new CountDownLatch(2);
        this.start(bindPort, waiter, bindCallBack, activeCallBack);
        try {
            // 等待服务成功启动。再继续后面的逻辑，1min超时
            if (!waiter.await(1, TimeUnit.MINUTES)) {
                LOG.warn(String.format("%s start time out [%d%s]", this.getClass().getSimpleName(), 1, TimeUnit.MINUTES));
            }
        } catch (InterruptedException e) {
            // 抛出异常
            throw new KcpInitException(e);
        }
    }

    /**
     * 启动服务
     *
     * @param bindPort       绑定端口
     * @param waiter         等待
     * @param bindCallBack   绑定回调
     * @param activeCallBack 激活回调
     */
    protected void start(int bindPort, CountDownLatch waiter, StartUpNettyServerCallBack bindCallBack, StartUpNettyServerCallBack activeCallBack) throws KcpInitException {
        // 初始线程组
        initGroups();
        // 初始并配置Netty启动类
        initConfigureBootStrap();
        // 配置连接参数
        applyConnectionOptions();

        ChannelFuture bindFuture = bootstrap.bind(bindPort).addListener((FutureListener<Void>) future -> {
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
            // 存ServerChannel
            Channel channel = bindFuture.channel();
            serverChannels.add(channel);
            // 计数
            waiter.countDown();
        });
    }

    /**
     * 初始线程组
     */
    protected void initGroups() {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();

        // 创建BOSS线程组
        int bossNum = netChannelConfig.getBossThreadNum();
        bossNum = bossNum == 0 ? SystemOS.CPU_NUM : bossNum;
        if (bossNum > 0) {
            bossGroup = nettyGroupChannel.createEventLoopGroup(bossNum);
        }
        // 创建IO线程组
        int ioNum = netChannelConfig.getIoThreadNum();
        ioNum = ioNum == 0 ? SystemOS.CPU_NUM : ioNum;
        if (ioNum > 0) {
            ioGroup = nettyGroupChannel.createEventLoopGroup(ioNum);
        }
    }

    /**
     * 初始化并配置BootStrap
     */
    protected abstract void initConfigureBootStrap();

    /**
     * 配置连接参数
     */
    protected abstract void applyConnectionOptions();

    /**
     * 初始channel链
     *
     * @param ch channel
     */
    protected abstract void initChannel(Channel ch) throws Exception;

    @Override
    public String toString() {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();
        return this.getClass().getSimpleName() + "{" +
                "bindPort=" + netChannelConfig.getBindPort() +
                ", bossGroup.num=" + netChannelConfig.getBossThreadNum() +
                ", ioGroup.num=" + netChannelConfig.getIoThreadNum() +
                '}';
    }
}
