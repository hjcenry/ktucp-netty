package com.hjcenry.server.udp;

import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.KcpListener;
import com.hjcenry.kcp.KcpOutput;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 16:24
 **/
public class UdpServerChannelHandler extends AbstractServerChannelHandler {

    public UdpServerChannelHandler(IChannelManager channelManager, ChannelConfig channelConfig, IMessageExecutorPool iMessageExecutorPool, KcpListener kcpListener, HashedWheelTimer hashedWheelTimer) {
        super(channelManager, channelConfig, iMessageExecutorPool, kcpListener, hashedWheelTimer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        ByteBuf readByteBuf = msg.content();
        // 获取KCP对象
        Ukcp ukcp = channelManager.getKcp(ctx.channel(), readByteBuf, msg.sender());
        ByteBuf byteBuf = msg.content();
        // 读消息
        channelRead0(ctx, msg, ukcp, byteBuf);
    }

    @Override
    protected KcpOutput getKcpOutput() {
        // UDP输出
        return new UdpOutPutImp();
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
