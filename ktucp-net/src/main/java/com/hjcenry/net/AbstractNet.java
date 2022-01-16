package com.hjcenry.net;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.INet;
import com.hjcenry.kcp.NetTypeEnum;
import com.hjcenry.system.SystemOS;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.log.KtucpLog;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 11:39
 **/
public abstract class AbstractNet implements INet {

    protected static final Logger logger = KtucpLog.logger;
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

    public AbstractNet(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        this.netId = netId;
        this.netTypeEnum = netTypeEnum;
        this.netConfigData = netConfigData;
        this.checkConfigData();
    }

    /**
     * 检测网络服务参数合法性
     */
    protected void checkConfigData() throws KtucpInitException {
        if (this.netConfigData == null) {
            throw new KtucpInitException("NetConfigData null");
        }
        if (this.netConfigData.getChannelConfig() == null) {
            throw new KtucpInitException("NetServer Channel Config can not be null");
        }
        if (this.netConfigData.getNetChannelConfig() == null) {
            throw new KtucpInitException("NetServer Net Channel Config can not be null");
        }
        if (this.netConfigData.getListener() == null) {
            throw new KtucpInitException("NetServer Listener can not be null");
        }
        if (this.netConfigData.getChannelManager() == null) {
            throw new KtucpInitException("NetServer ChannelManager can not be null");
        }
        if (this.netConfigData.getHashedWheelTimer() == null) {
            throw new KtucpInitException("NetServer HashedWheelTimer can not be null");
        }
        if (this.netConfigData.getMessageExecutorPool() == null) {
            throw new KtucpInitException("NetServer MessageExecutorPool can not be null");
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
     * @throws Exception 初始异常
     */
    protected abstract void initChannel(Channel ch) throws Exception;

    @Override
    public void stop() {
        IChannelManager channelManager = netConfigData.getChannelManager();
        channelManager.getAll().forEach(Uktucp::close);
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
