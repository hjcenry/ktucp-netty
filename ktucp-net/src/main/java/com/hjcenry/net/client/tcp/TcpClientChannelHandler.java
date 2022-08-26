package com.hjcenry.net.client.tcp;

import com.hjcenry.kcp.*;
import com.hjcenry.net.client.AbstractClientChannelHandler;
import com.hjcenry.net.NetChannelConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

/**
 * TCP客户端处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 14:57
 **/
@ChannelHandler.Sharable
public class TcpClientChannelHandler extends AbstractClientChannelHandler {

    /**
     * TCP有连接存在，可通过连接映射KCP对象
     */
    private final HandlerChannelManager clientChannelManager;

    public TcpClientChannelHandler(INet net, IChannelManager channelManager, ChannelConfig channelConfig, NetChannelConfig netChannelConfig) {
        super(net, channelManager, channelConfig, netChannelConfig);
        this.clientChannelManager = new HandlerChannelManager();
    }

    @Override
    protected ByteBuf getReadByteBuf(Channel channel, Object msg) {
        return (ByteBuf) msg;
    }

    @Override
    protected Uktucp getReadUkcp(Channel channel, Object msg) {
        // 获取KCP对象
        // 先从Channel获取
        Uktucp uktucp = this.clientChannelManager.getKcp(channel);
        if (uktucp != null) {
            return uktucp;
        }
        // 从convId获取
        ByteBuf byteBuf = (ByteBuf) msg;
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        return this.channelManager.getKcp(byteBuf, remoteAddress);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel channel = ctx.channel();
        // 移除绑定
        this.clientChannelManager.remove(channel);
    }

    @Override
    protected Uktucp getUkcpByChannel(Channel channel) {
        // 通过TCP Channel获取KCP对象
        return this.clientChannelManager.getKcp(channel);
    }
}
