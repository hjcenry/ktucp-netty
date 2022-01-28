package com.hjcenry.net.server.tcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.threadpool.IMessageExecutor;
import com.hjcenry.threadpool.IMessageExecutorPool;
import com.hjcenry.net.server.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IChannelManager;
import com.hjcenry.kcp.HandlerChannelManager;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.NetChannelConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
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
@ChannelHandler.Sharable
public class TcpServerChannelHandler extends AbstractServerChannelHandler {

    /**
     * TCP有连接存在，可通过连接映射KCP对象
     */
    private final HandlerChannelManager serverChannelManager;

    public TcpServerChannelHandler(int netId, IChannelManager channelManager,
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
        this.serverChannelManager = new HandlerChannelManager();
    }

    @Override
    protected Uktucp createUkcp(int conv, Channel channel, Object readObject, IMessageExecutor iMessageExecutor) {
        Uktucp uktucp = super.createUkcp(conv, channel, readObject, iMessageExecutor);
        // 添加TCP通道管理
        this.serverChannelManager.addKcp(uktucp, channel);
        return uktucp;
    }

    @Override
    protected void channelReadFromUktucp(Channel channel, Object readObject, Uktucp uktucp, ByteBuf byteBuf) {
        super.channelReadFromUktucp(channel, readObject, uktucp, byteBuf);
        // 添加TCP通道管理
        Uktucp oldUktucp = this.serverChannelManager.getKcp(channel);
        if (oldUktucp == null) {
            this.serverChannelManager.addKcp(uktucp, channel);
        }
    }

    @Override
    protected ByteBuf getReadByteBuf(Channel channel, Object msg) {
        return (ByteBuf) msg;
    }

    @Override
    protected Uktucp getReadUkcp(Channel channel, Object msg) {
        // 获取KCP对象
        // TCP连接直接通过Channel管理中取对象，可有效检测数据包来源
        Uktucp uktucp = this.serverChannelManager.getKcp(channel);
        if (uktucp != null) {
            return uktucp;
        }
        // 没有TCP连接，就只能从conv取了
        ByteBuf byteBuf = (ByteBuf) msg;
        InetSocketAddress remoteAddress = this.getRemoteAddress(channel, msg);
        uktucp = this.channelManager.getKcp(byteBuf, remoteAddress);
        if (uktucp == null) {
            // 确实是找不到这个用户了
            return null;
        }
        Channel curChannel = uktucp.user().getNetChannel(this.netId);
        // convId取出来的人，和当前Channel不是同一个
        if (curChannel != null && curChannel != channel) {
            // 如果convId的用户从来没登陆过TCP，或者用户的TCP网络掉线了，那么理论上，其他人也是可以利用这个间隙伪造正常用户的TCP消息的，
            // 这里只做一层TCP安全检测兜底，如果依赖这里的逻辑进行假消息检测，需要外层做好保证用户的第一条消息一定是TCP
            // 另外一种解决方案：应用层自己对消息包做一层封装，服务器为每个用户分配一个token，客户端拿这个token来访问网络
            return null;
        }
        return uktucp;
    }

    @Override
    protected boolean checkCreateUktucp(Channel channel, Object readObject, int convId, ByteBuf byteBuf) {
        // TCP的Uktucp对象是通过channel取的，
        // 如果存在造假情况，那数据包中传的convId有可能已经存在
        // 因此这里通过convId再次取一下Uktucp，如果已存在，并且不是同一个Channel，则不创建新的Uktucp对象
        InetSocketAddress remoteAddress = getRemoteAddress(channel, readObject);
        // 检测Manager中的KCP对象是否存在
        Uktucp uktucp = channelManager.getKcp(byteBuf, remoteAddress);
        if (uktucp == null) {
            return true;
        }
        Channel curChannel = uktucp.user().getNetChannel(this.netId);
        if (curChannel != null && curChannel != channel) {
            // convId取出来的人，身上有channel，并且不是同一个Channel
            if (KtucpLog.logger.isWarnEnabled()) {
                KtucpLog.logger.warn(String.format("Uktucp.%d.may.be.attack.by.%s", convId, channel));
            }
            return false;
        }
        return true;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel channel = ctx.channel();
        // 移除绑定
        this.serverChannelManager.remove(channel);
    }

    @Override
    protected Uktucp getUkcpByChannel(Channel channel) {
        // 通过TCP Channel获取KCP对象
        return this.serverChannelManager.getKcp(channel);
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
