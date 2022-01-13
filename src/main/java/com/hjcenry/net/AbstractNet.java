package com.hjcenry.net;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.log.KcpLog;
import com.hjcenry.net.server.NetTypeEnum;
import com.hjcenry.system.SystemOS;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 11:39
 **/
public abstract class AbstractNet implements INet {

    protected static final Logger logger = KcpLog.logger;
    /**
     * IO线程组
     */
    protected EventLoopGroup ioGroup;
    /**
     * 网络id
     */
    protected int netId;
    /**
     * 网络配置数据
     */
    protected NetConfigData netConfigData;
    /**
     * Netty网络通道类型
     */
    protected NettyGroupChannel nettyGroupChannel = NettyGroupChannel.getNettyGroupChannel();

    protected NetTypeEnum netTypeEnum;

    public AbstractNet(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KcpInitException {
        this.netId = netId;
        this.netTypeEnum = netTypeEnum;
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

    /**
     * 初始线程组
     */
    protected void initGroups() {
        NetChannelConfig netChannelConfig = this.netConfigData.getNetChannelConfig();

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
    public void stop() {
        IChannelManager channelManager = netConfigData.getChannelManager();
        channelManager.getAll().forEach(Ukcp::close);
        if (ioGroup != null) {
            ioGroup.shutdownGracefully();
        }
    }

    @Override
    public int getNetId() {
        return netId;
    }

    @Override
    public NetTypeEnum getNetTypeEnum() {
        return netTypeEnum;
    }
}
