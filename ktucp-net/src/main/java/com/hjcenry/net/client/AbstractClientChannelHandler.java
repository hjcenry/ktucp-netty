package com.hjcenry.net.client;

import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.net.AbstractChannelHandler;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.tcp.INettyChannelEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

/**
 * 抽象客户端channel处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:21
 **/
public abstract class AbstractClientChannelHandler extends AbstractChannelHandler {

    protected static final Logger logger = KtucpLog.logger;

    public AbstractClientChannelHandler(int netId, IChannelManager channelManager, ChannelConfig channelConfig, NetChannelConfig netChannelConfig) {
        super(netId, channelManager, channelConfig, netChannelConfig);
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
            Uktucp uktucp = this.getUkcpByChannel(channel);
            channelEvent.onChannelInactive(channel, uktucp);
        }
    }

    @Override
    protected void channelRead0(Channel channel, Object readObject, Uktucp uktucp, ByteBuf byteBuf) {
        if (uktucp != null) {
            // 如果连接提前被关闭，这里有可能取空
            uktucp.read(byteBuf);
        }
    }
}
