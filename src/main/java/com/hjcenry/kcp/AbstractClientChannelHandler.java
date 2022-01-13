package com.hjcenry.kcp;

import com.hjcenry.log.KcpLog;
import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.tcp.INettyChannelEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象客户端channel处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:21
 **/
public abstract class AbstractClientChannelHandler extends AbstractChannelHandler {

    protected static final Logger logger = KcpLog.logger;

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
            Ukcp ukcp = this.getUkcpByChannel(channel);
            channelEvent.onChannelInactive(channel, ukcp);
        }
    }

    @Override
    protected void channelRead0(Channel channel, Object readObject, Ukcp ukcp, ByteBuf byteBuf) {
        ukcp.read(byteBuf);
    }
}
