package com.hjcenry.kcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 抽象服务端channel处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:21
 **/
public abstract class AbstractServerChannelHandler extends AbstractChannelHandler {

    /**
     * 消息线程池
     */
    protected IMessageExecutorPool iMessageExecutorPool;
    /**
     * KCP监听
     */
    protected KtucpListener ktucpListener;
    /**
     * 时间轮
     */
    protected HashedWheelTimer hashedWheelTimer;
    /**
     * 消息编码器
     */
    protected IMessageEncoder messageEncoder;
    /**
     * 消息解码器
     */
    protected IMessageDecoder messageDecoder;

    public AbstractServerChannelHandler(int netId,
                                        IChannelManager channelManager,
                                        ChannelConfig channelConfig,
                                        NetChannelConfig netChannelConfig,
                                        IMessageExecutorPool iMessageExecutorPool,
                                        KtucpListener ktucpListener,
                                        HashedWheelTimer hashedWheelTimer,
                                        IMessageEncoder messageEncoder,
                                        IMessageDecoder messageDecoder) {
        super(netId, channelManager, channelConfig, netChannelConfig);
        this.iMessageExecutorPool = iMessageExecutorPool;
        this.ktucpListener = ktucpListener;
        this.hashedWheelTimer = hashedWheelTimer;
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;
    }

    /**
     * 绑定通道
     *
     * @param uktucp          kcp对象
     * @param channel       通道
     * @param localAddress  本地地址
     * @param remoteAddress 远端地址
     */
    protected void bindChannel(Uktucp uktucp, Channel channel, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        User user = uktucp.user();
        UserNetManager userNetManager = user.getUserNetManager();
        userNetManager.addNetInfo(this.netId, channel, localAddress, remoteAddress);
    }

    @Override
    protected void channelRead0(Channel channel, Object readObject, Uktucp uktucp, ByteBuf byteBuf) {
        if (uktucp != null) {
            User user = uktucp.user();
            //绑定当前网络
            uktucp.changeCurrentNetId(this.netId);
            //每次收到消息重绑定地址
            InetSocketAddress remoteAddress = getRemoteAddress(channel, readObject);
            user.changeRemoteAddress(this.netId, remoteAddress);
            // 读消息
            uktucp.read(byteBuf);
            return;
        }

        //如果是新连接第一个包的sn必须为0
        int sn = getSn(byteBuf, channelConfig);
        if (sn != 0) {
            ReferenceCountUtil.release(byteBuf);
            return;
        }
        // 获取一个处理器
        IMessageExecutor iMessageExecutor = iMessageExecutorPool.getMessageExecutor();

        // 创建kcp对象
        Uktucp newUktucp = createUkcp(channel, readObject, byteBuf, iMessageExecutor);

        iMessageExecutor.execute(() -> {
            try {
                newUktucp.getKcpListener().onConnected(this.netId, newUktucp);
            } catch (Throwable throwable) {
                newUktucp.getKcpListener().handleException(throwable, newUktucp);
            }
        });

        // 读消息
        newUktucp.read(byteBuf);

        ScheduleTask scheduleTask = new ScheduleTask(iMessageExecutor, newUktucp, hashedWheelTimer, channelConfig.isKcpIdleTimeoutClose());
        hashedWheelTimer.newTimeout(scheduleTask, newUktucp.getInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * 创建KCP 对象
     *
     * @param channel          通道
     * @param readObject       读消息对象
     * @param readByteBuf      读消息
     * @param iMessageExecutor 处理器
     * @return kcp对象
     */
    protected Uktucp createUkcp(Channel channel, Object readObject, ByteBuf readByteBuf, IMessageExecutor iMessageExecutor) {
        KtucpOutput ktucpOutput = this.getKcpOutput();
        Uktucp newUktucp = new Uktucp(ktucpOutput, ktucpListener, iMessageExecutor, this.channelConfig, this.channelManager, this.messageEncoder, this.messageDecoder);
        // 创建user
        User user = new User(this.netId, this.channelConfig.getNetNum());
        newUktucp.user(user);
        // 服务端模式
        newUktucp.setServerMode();
        // 绑定通道
        InetSocketAddress localAddress = getLocalAddress(channel, readObject);
        InetSocketAddress remoteAddress = getRemoteAddress(channel, readObject);
        this.bindChannel(newUktucp, channel, localAddress, remoteAddress);
        // 绑定convId
        int conv = channelManager.getConvIdByByteBuf(readByteBuf);
        if (conv > 0) {
            newUktucp.setConv(conv);
        }
        // 添加ukcp管理
        channelManager.addKcp(newUktucp);
        return newUktucp;
    }

    /**
     * 获取KCP输出方法
     *
     * @return KCP输出方法
     */
    protected KtucpOutput getKcpOutput() {
        return new KtucpOutPutImp();
    }

    /**
     * 获取本地地址
     *
     * @param channel    通道
     * @param readObject 读取消息
     * @return 本地地址
     */
    protected abstract InetSocketAddress getLocalAddress(Channel channel, Object readObject);

    /**
     * 获取远程地址
     *
     * @param channel    通道
     * @param readObject 读取消息
     * @return 远程地址
     */
    protected abstract InetSocketAddress getRemoteAddress(Channel channel, Object readObject);

    private int getSn(ByteBuf byteBuf, ChannelConfig channelConfig) {
        int headerSize = 0;
        if (channelConfig.getFecAdapt() != null) {
            headerSize += Fec.fecHeaderSizePlus2;
        }
        return byteBuf.getIntLE(byteBuf.readerIndex() + Kcp.IKCP_SN_OFFSET + headerSize);
    }
}
