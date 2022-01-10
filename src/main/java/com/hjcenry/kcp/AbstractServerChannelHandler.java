package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Fec;
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

    static final Logger logger = LoggerFactory.getLogger(AbstractServerChannelHandler.class);

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

    public AbstractServerChannelHandler(IChannelManager channelManager, ChannelConfig channelConfig, IMessageExecutorPool iMessageExecutorPool, KcpListener kcpListener, HashedWheelTimer hashedWheelTimer) {
        this.channelManager = channelManager;
        this.channelConfig = channelConfig;
        this.iMessageExecutorPool = iMessageExecutorPool;
        this.kcpListener = kcpListener;
        this.hashedWheelTimer = hashedWheelTimer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("", cause);
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object readObject, Ukcp ukcp, ByteBuf byteBuf) {
        if (ukcp != null) {
            User user = ukcp.user();
            //每次收到消息重绑定地址
            InetSocketAddress remoteAddress = getRemoteAddress(ctx.channel(), readObject);
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
        Ukcp newUkcp = createUkcp(ctx.channel(), readObject, byteBuf, iMessageExecutor);

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
        Ukcp newUkcp = new Ukcp(kcpOutput, kcpListener, iMessageExecutor, channelConfig, channelManager);
        // 创建user
        InetSocketAddress localAddress = getLocalAddress(channel, readObject);
        InetSocketAddress remoteAddress = getRemoteAddress(channel, readObject);

        User user = new User(channel, remoteAddress, localAddress);
        newUkcp.user(user);
        // 绑定convId
        int conv = channelManager.getConvIdByByteBuf(readByteBuf);
        if (conv > 0) {
            newUkcp.setConv(conv);
        }
        // 添加ukcp管理
        channelManager.addKcp(newUkcp);
        return newUkcp;
    }

    /**
     * 获取KCP输出方法
     *
     * @return KCP输出方法
     */
    protected abstract KcpOutput getKcpOutput();

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
