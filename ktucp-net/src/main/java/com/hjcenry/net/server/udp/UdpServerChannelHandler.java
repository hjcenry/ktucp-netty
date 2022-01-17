package com.hjcenry.net.server.udp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.threadpool.IMessageExecutorPool;
import com.hjcenry.net.server.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.NetChannelConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;

/**
 * UDP没有比较安全可靠的能在网络层检测消息可靠性的方法
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 16:24
 **/
@ChannelHandler.Sharable
public class UdpServerChannelHandler extends AbstractServerChannelHandler {

    public UdpServerChannelHandler(int netId, IChannelManager channelManager,
                                   ChannelConfig channelConfig,
                                   NetChannelConfig netChannelConfig, IMessageExecutorPool iMessageExecutorPool,
                                   KtucpListener ktucpListener,
                                   HashedWheelTimer hashedWheelTimer,
                                   IMessageEncoder messageEncoder,
                                   IMessageDecoder messageDecoder) {
        super(netId, channelManager,
                channelConfig,
                netChannelConfig,
                iMessageExecutorPool,
                ktucpListener,
                hashedWheelTimer,
                messageEncoder,
                messageDecoder);
    }

    @Override
    protected Uktucp getReadUkcp(Channel channel, Object object) {
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
