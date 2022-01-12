package com.hjcenry.kcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.server.tcp.INettyChannelEvent;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.HashedWheelTimer;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 抽象服务channel处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:21
 **/
public abstract class AbstractServerChannelHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractServerChannelHandler.class);

    /**
     * Channel管理器
     */
    protected IChannelManager channelManager;
    /**
     * 配置
     */
    protected ChannelConfig channelConfig;
    /**
     * 消息线程池
     */
    protected IMessageExecutorPool iMessageExecutorPool;
    /**
     * KCP监听
     */
    protected KcpListener kcpListener;
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
    /**
     * Netty事件
     */
    protected INettyChannelEvent nettyChannelEvent;

    protected int netId;

    public AbstractServerChannelHandler(int netId, IChannelManager channelManager,
                                        ChannelConfig channelConfig,
                                        IMessageExecutorPool iMessageExecutorPool,
                                        KcpListener kcpListener,
                                        HashedWheelTimer hashedWheelTimer,
                                        IMessageEncoder messageEncoder,
                                        IMessageDecoder messageDecoder) {
        this.netId = netId;
        this.channelManager = channelManager;
        this.channelConfig = channelConfig;
        this.iMessageExecutorPool = iMessageExecutorPool;
        this.kcpListener = kcpListener;
        this.hashedWheelTimer = hashedWheelTimer;
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (this.nettyChannelEvent != null) {
            // 掉线事件
            Channel channel = ctx.channel();
            Ukcp ukcp = this.getUkcpByChannel(channel);
            this.nettyChannelEvent.onChannelActive(channel);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (this.nettyChannelEvent != null) {
            // 掉线事件
            Channel channel = ctx.channel();
            Ukcp ukcp = this.getUkcpByChannel(channel);
            this.nettyChannelEvent.onChannelInactive(channel, ukcp);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        // 获取KCP对象
        Ukcp ukcp = this.getReadUkcp(channel, msg);
        if (this.nettyChannelEvent != null) {
            // 读事件
            this.nettyChannelEvent.onChannelRead(channel, ukcp);
        }
        // 获取消息
        ByteBuf byteBuf = this.getReadByteBuf(channel, msg);
        // 读消息
        this.channelRead0(channel, msg, ukcp, byteBuf);
    }

    /**
     * 读取ByteBuf
     *
     * @param channel 通道
     * @param msg     消息
     * @return ByteBuf
     */
    protected abstract ByteBuf getReadByteBuf(Channel channel, Object msg);

    /**
     * 读取Ukcp
     *
     * @param channel 通道
     * @param msg     消息
     * @return Ukcp
     */
    protected abstract Ukcp getReadUkcp(Channel channel, Object msg);

    /**
     * 绑定通道
     *
     * @param ukcp    kcp对象
     * @param channel 通道
     */
    protected void bindChannel(Ukcp ukcp, Channel channel) {
        ukcp.user().addChannel(this.netId, channel);
    }

    protected void channelRead0(Channel channel, Object readObject, Ukcp ukcp, ByteBuf byteBuf) {
        if (ukcp != null) {
            User user = ukcp.user();

            //每次收到消息重绑定地址
            InetSocketAddress remoteAddress = getRemoteAddress(channel, readObject);
            user.setRemoteAddress(remoteAddress);
            ukcp.read(byteBuf);
            return;
        }

        //如果是新连接第一个包的sn必须为0
        int sn = getSn(byteBuf, channelConfig);
        if (sn != 0) {
            ReferenceCountUtil.release(byteBuf);
            return;
        }
        // 获取一个处理器
        IMessageExecutor iMessageExecutor = iMessageExecutorPool.getIMessageExecutor();

        // 创建kcp对象
        Ukcp newUkcp = createUkcp(channel, readObject, byteBuf, iMessageExecutor);

        iMessageExecutor.execute(() -> {
            try {
                newUkcp.getKcpListener().onConnected(newUkcp);
            } catch (Throwable throwable) {
                newUkcp.getKcpListener().handleException(throwable, newUkcp);
            }
        });

        // 读消息
        newUkcp.read(byteBuf);

        ScheduleTask scheduleTask = new ScheduleTask(iMessageExecutor, newUkcp, hashedWheelTimer);
        hashedWheelTimer.newTimeout(scheduleTask, newUkcp.getInterval(), TimeUnit.MILLISECONDS);
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
    protected Ukcp createUkcp(Channel channel, Object readObject, ByteBuf readByteBuf, IMessageExecutor iMessageExecutor) {
        KcpOutput kcpOutput = this.getKcpOutput();
        Ukcp newUkcp = new Ukcp(kcpOutput, kcpListener, iMessageExecutor, this.channelConfig, this.channelManager, this.messageEncoder, this.messageDecoder);
        // 创建user
        InetSocketAddress localAddress = getLocalAddress(channel, readObject);
        InetSocketAddress remoteAddress = getRemoteAddress(channel, readObject);

        User user = new User(remoteAddress, localAddress, this.channelConfig.getNetNum());
        newUkcp.user(user);

        // 绑定通道
        this.bindChannel(newUkcp, channel);

        // 绑定convId
        int conv = channelManager.getConvIdByByteBuf(readByteBuf);
        if (conv > 0) {
            newUkcp.setConv(conv);
        }
        // 添加ukcp管理
        channelManager.addKcp(newUkcp, channel);
        return newUkcp;
    }

    /**
     * 获取KCP输出方法
     *
     * @return KCP输出方法
     */
    protected KcpOutput getKcpOutput() {
        return new KcpOutPutImp(this.netId);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (logger.isWarnEnabled()) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                cause.printStackTrace(pw);
            }
            logger.warn("An exception was thrown by a user handler's exceptionCaught() " +
                    "method while handling the following exception:" + sw.toString());
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (this.nettyChannelEvent != null) {
            // 触发事件
            Ukcp ukcp = this.getUkcpByChannel(ctx.channel());
            this.nettyChannelEvent.onChannelRead(ctx.channel(), ukcp);
        }
    }

    /**
     * 通过Channel取Ukcp
     *
     * @param channel 通道
     * @return Ukcp
     */
    protected Ukcp getUkcpByChannel(Channel channel) {
        return null;
    }

}
