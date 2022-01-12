package com.hjcenry.server.tcp;

import com.hjcenry.kcp.Ukcp;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * Netty事件触发
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 18:43
 */
public interface INettyChannelEvent {

    public void onChannelActive(Channel channel);

    public void onChannelInactive(Channel channel, Ukcp ukcp);

    public void onChannelRead(Channel channel, Ukcp ukcp);

    public void onUserEventTriggered(Channel channel, Object evt);
}
