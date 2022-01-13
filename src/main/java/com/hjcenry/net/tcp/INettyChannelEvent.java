package com.hjcenry.net.tcp;

import com.hjcenry.kcp.Ukcp;
import io.netty.channel.Channel;

/**
 * Netty事件触发
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 18:43
 */
public interface INettyChannelEvent {

    /**
     * 通道激活事件
     *
     * @param channel 通道
     */
    public void onChannelActive(Channel channel);

    /**
     * 通道关闭事件
     *
     * @param channel 通道
     * @param ukcp    kcp对象 <b>仅TCP连接能通过channel找到KCP对象</b>
     */
    public void onChannelInactive(Channel channel, Ukcp ukcp);

    /**
     * 读取消息事件
     *
     * @param channel 通道
     * @param ukcp    kcp对象 可能为null
     */
    public void onChannelRead(Channel channel, Ukcp ukcp);

    /**
     * 触发事件
     *
     * @param channel 通道
     * @param ukcp    KCP对象 <b>仅TCP连接能通过channel找到KCP对象</b>
     * @param evt     事件
     */
    public void onUserEventTriggered(Channel channel, Ukcp ukcp, Object evt);
}
