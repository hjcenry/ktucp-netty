package com.hjcenry.server.tcp;

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
import io.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;

/**
 * TCP服务处理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 16:46
 **/
public class TcpServerChannelHandler extends AbstractServerChannelHandler {

    public TcpServerChannelHandler(IChannelManager channelManager, ChannelConfig channelConfig, IMessageExecutorPool iMessageExecutorPool, KcpListener kcpListener, HashedWheelTimer hashedWheelTimer) {
        super(channelManager, channelConfig, iMessageExecutorPool, kcpListener, hashedWheelTimer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        // 获取KCP对象
        Channel channel = ctx.channel();
        Ukcp ukcp = channelManager.getKcp(ctx.channel(), byteBuf, (InetSocketAddress) channel.remoteAddress());
        // 读消息
        this.channelRead0(ctx, msg, ukcp, byteBuf);
    }

    @Override
    protected KcpOutput getKcpOutput() {
        // TCP输出
        return new TcpOutPutImp();
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
