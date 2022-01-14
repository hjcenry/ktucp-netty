package com.hjcenry.net.client.udp;

import com.hjcenry.kcp.AbstractClientChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.net.NetChannelConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * udp客户端处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/13 14:58
 **/
@ChannelHandler.Sharable
public class UdpClientChannelHandler extends AbstractClientChannelHandler {

    public UdpClientChannelHandler(int netId, IChannelManager channelManager, ChannelConfig channelConfig, NetChannelConfig netChannelConfig) {
        super(netId, channelManager, channelConfig, netChannelConfig);
    }

    @Override
    protected Ukcp getReadUkcp(Channel channel, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        ByteBuf readByteBuf = msg.content();
        // 获取KCP对象
        return channelManager.getKcp(readByteBuf, msg.sender());
    }

    @Override
    protected ByteBuf getReadByteBuf(Channel channel, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        return msg.content();
    }
}
