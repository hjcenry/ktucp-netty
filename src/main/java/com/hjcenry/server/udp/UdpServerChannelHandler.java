package com.hjcenry.server.udp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KcpOutput;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 16:24
 **/
@ChannelHandler.Sharable
public class UdpServerChannelHandler extends AbstractServerChannelHandler {

    public UdpServerChannelHandler(int netId, IChannelManager channelManager,
                                   ChannelConfig channelConfig,
                                   IMessageExecutorPool iMessageExecutorPool,
                                   KcpListener kcpListener,
                                   HashedWheelTimer hashedWheelTimer,
                                   IMessageEncoder messageEncoder,
                                   IMessageDecoder messageDecoder) {
        super(netId, channelManager,
                channelConfig,
                iMessageExecutorPool,
                kcpListener,
                hashedWheelTimer,
                messageEncoder,
                messageDecoder);
    }

    @Override
    protected Ukcp getReadUkcp(Channel channel, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        ByteBuf readByteBuf = msg.content();
        // 获取KCP对象
        return channelManager.getKcp(channel, readByteBuf, msg.sender());
    }

    @Override
    protected ByteBuf getReadByteBuf(Channel channel, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        return msg.content();
    }

    @Override
    protected InetSocketAddress getLocalAddress(Channel channel, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        return msg.recipient();
    }

    @Override
    protected InetSocketAddress getRemoteAddress(Channel channel, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        return msg.sender();
    }
}
