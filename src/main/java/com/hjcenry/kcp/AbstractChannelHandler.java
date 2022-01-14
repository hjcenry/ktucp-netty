package com.hjcenry.kcp;

import com.hjcenry.log.KcpLog;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.tcp.INettyChannelEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 抽象Channel处理
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 14:43
 **/
public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger logger = KcpLog.logger;
    /**
     * Channel管理器
     */
    protected IChannelManager channelManager;
    /**
     * 配置
     */
    protected ChannelConfig channelConfig;
    /**
     * 网络配置
     */
    protected NetChannelConfig netChannelConfig;
    /**
     * 网络id
     */
    protected int netId;

    public AbstractChannelHandler(int netId, IChannelManager channelManager, ChannelConfig channelConfig, NetChannelConfig netChannelConfig) {
        this.netId = netId;
        this.channelManager = channelManager;
        this.channelConfig = channelConfig;
        this.netChannelConfig = netChannelConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        INettyChannelEvent channelEvent = this.netChannelConfig.getNettyEventTrigger();
        if (channelEvent != null) {
            // 掉线事件
            Channel channel = ctx.channel();
            channelEvent.onChannelActive(channel);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        INettyChannelEvent channelEvent = this.netChannelConfig.getNettyEventTrigger();
        if (channelEvent != null) {
            // 掉线事件
            Channel channel = ctx.channel();
            Ukcp ukcp = this.getUkcpByChannel(channel);
            channelEvent.onChannelInactive(channel, ukcp);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object readObject) throws Exception {
        Channel channel = ctx.channel();
        // 获取KCP对象
        Ukcp ukcp = this.getReadUkcp(channel, readObject);

        // 获取消息
        ByteBuf byteBuf = this.getReadByteBuf(channel, readObject);

        INettyChannelEvent channelEvent = this.netChannelConfig.getNettyEventTrigger();
        if (channelEvent != null) {
            // 读事件
            channelEvent.onChannelRead(channel, ukcp, byteBuf);
        }

        // 读消息
        this.channelRead0(channel, readObject, ukcp, byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (logger.isWarnEnabled()) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                cause.printStackTrace(pw);
            }
            logger.warn("An exception was thrown by a user handler's exceptionCaught() " +
                    "method while handling the following exception:" + sw);
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        INettyChannelEvent channelEvent = this.netChannelConfig.getNettyEventTrigger();
        if (channelEvent != null) {
            // 触发事件
            Channel channel = ctx.channel();
            // 仅TCP连接能通过Channel取出KCP对象
            Ukcp ukcp = this.getUkcpByChannel(channel);
            channelEvent.onUserEventTriggered(channel, ukcp, evt);
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

    /**
     * 读消息
     *
     * @param channel    通道
     * @param readObject 数据
     * @param ukcp       kcp对象
     * @param byteBuf    字节流
     */
    protected abstract void channelRead0(Channel channel, Object readObject, Ukcp ukcp, ByteBuf byteBuf);

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
}
