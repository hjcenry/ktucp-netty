package com.hjcenry.server.tcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.ServerHandlerChannelManager;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;

/**
 * TCP服务处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 16:46
 **/
@ChannelHandler.Sharable
public class TcpServerChannelHandler extends AbstractServerChannelHandler {

    /**
     * TCP有连接存在，可通过连接映射KCP对象
     */
    private final ServerHandlerChannelManager clientChannelManager;

    public TcpServerChannelHandler(int netId, IChannelManager channelManager,
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
        this.clientChannelManager = new ServerHandlerChannelManager();
    }

    @Override
    protected Ukcp createUkcp(Channel channel, Object readObject, ByteBuf readByteBuf, IMessageExecutor iMessageExecutor) {
        Ukcp ukcp = super.createUkcp(channel, readObject, readByteBuf, iMessageExecutor);
        // 添加TCP通道管理
        this.clientChannelManager.addKcp(ukcp, channel);
        return ukcp;
    }

    @Override
    protected ByteBuf getReadByteBuf(Channel channel, Object msg) {
        return (ByteBuf) msg;
    }

    @Override
    protected Ukcp getReadUkcp(Channel channel, Object msg) {
        // 获取KCP对象
        return this.clientChannelManager.getKcp(channel);
    }

    @Override
    protected Ukcp getUkcpByChannel(Channel channel) {
        // 通过TCP Channel获取KCP对象
        return this.clientChannelManager.getKcp(channel);
    }

    @Override
    protected InetSocketAddress getLocalAddress(Channel channel, Object readObject) {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    protected InetSocketAddress getRemoteAddress(Channel channel, Object readObject) {
        return (InetSocketAddress) channel.remoteAddress();
    }
}
