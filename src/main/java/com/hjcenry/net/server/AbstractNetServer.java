package com.hjcenry.net.server;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.net.AbstractNet;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.callback.StartUpNettyServerCallBack;
import com.hjcenry.system.SystemOS;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 抽象网络服务
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:02
 **/
public abstract class AbstractNetServer extends AbstractNet implements INetServer {

    /**
     * 启动器
     */
    protected AbstractBootstrap<?, ?> bootstrap;
    /**
     * BOSS线程组
     */
    protected EventLoopGroup bossGroup;

    public AbstractNetServer(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        super(netId, netTypeEnum, netConfigData);
    }

    @Override
    public void start() throws KtucpInitException {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();

        int bindPort = netChannelConfig.getBindPort();
        if (bindPort < 0) {
            throw new KtucpInitException(String.format("%s start failed , port is [%d]", this.getClass().getSimpleName(), bindPort));
        }

        StartUpNettyServerCallBack bindCallBack = netChannelConfig.getBindCallback();
        StartUpNettyServerCallBack activeCallBack = netChannelConfig.getActiveCallback();

        String serverClassName = this.getClass().getSimpleName();
        // 绑定端口回调
        bindCallBack = bindCallBack != null ? bindCallBack : new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future, int netId) {
                if (future == null || future.isSuccess()) {
                    if (logger.isInfoEnabled()) {
                        logger.info(serverClassName + " Net bind start success at port: " + bindPort);
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(serverClassName + " Net bind failed at port: " + bindPort);
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
                        logger.info(serverClassName + " Net active start success at port: " + bindPort);
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(serverClassName + " Net active failed at port: " + bindPort);
                    }
                }
            }
        };
        // 需要等待两个callback都成功执行
        final CountDownLatch waiter = new CountDownLatch(2);
        this.start(bindPort, waiter, bindCallBack, activeCallBack);
        try {
            // 等待服务成功启动。再继续后面的逻辑，1min超时
            if (!waiter.await(1, TimeUnit.MINUTES)) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("%s start time out [%d%s]", this.getClass().getSimpleName(), 1, TimeUnit.MINUTES));
                }
            }
        } catch (InterruptedException e) {
            // 抛出异常
            throw new KtucpInitException(e);
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
    protected void start(int bindPort, CountDownLatch waiter, StartUpNettyServerCallBack bindCallBack, StartUpNettyServerCallBack activeCallBack) throws KtucpInitException {
        // 初始线程组
        initGroups();
        // 初始并配置Netty启动类
        initConfigureBootStrap();
        // 配置连接参数
        applyConnectionOptions();

        ChannelFuture bindFuture = bootstrap.bind(bindPort).addListener((FutureListener<Void>) future -> {
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
            // 计数
            waiter.countDown();
        });
    }

    /**
     * 初始线程组
     */
    @Override
    protected void initGroups() {
        super.initGroups();
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();
        // 创建BOSS线程组
        int bossNum = netChannelConfig.getBossThreadNum();
        bossNum = bossNum == 0 ? SystemOS.CPU_NUM : bossNum;
        if (bossNum > 0) {
            bossGroup = nettyGroupChannel.createEventLoopGroup(bossNum);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }

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
