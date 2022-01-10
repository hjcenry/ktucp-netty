package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Fec;
import com.hjcenry.server.udp.UdpOutPutImp;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by JinMiao
 * 2018/9/20.
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private IChannelManager channelManager;

    private ChannelConfig channelConfig;

    private IMessageExecutorPool iMessageExecutorPool;

    private KcpListener kcpListener;

    private HashedWheelTimer hashedWheelTimer;

    public ServerChannelHandler(IChannelManager channelManager, ChannelConfig channelConfig, IMessageExecutorPool iMessageExecutorPool, KcpListener kcpListener, HashedWheelTimer hashedWheelTimer) {
        this.channelManager = channelManager;
        this.channelConfig = channelConfig;
        this.iMessageExecutorPool = iMessageExecutorPool;
        this.kcpListener = kcpListener;
        this.hashedWheelTimer = hashedWheelTimer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("", cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        final ChannelConfig channelConfig = this.channelConfig;
        DatagramPacket msg = (DatagramPacket) object;
        ByteBuf readByteBuf = msg.content();

        Ukcp ukcp = channelManager.getKcp(ctx.channel(), readByteBuf, msg.sender());

        if (ukcp != null) {
            User user = ukcp.user();
            //每次收到消息重绑定地址
            user.setRemoteAddress(msg.sender());
            ukcp.read(readByteBuf);
            return;
        }

        //如果是新连接第一个包的sn必须为0
        int sn = getSn(readByteBuf, channelConfig);
        if (sn != 0) {
            msg.release();
            return;
        }
        IMessageExecutor iMessageExecutor = iMessageExecutorPool.getIMessageExecutor();
        KcpOutput kcpOutput = new UdpOutPutImp();
        Ukcp newUkcp = new Ukcp(kcpOutput, kcpListener, iMessageExecutor, channelConfig, channelManager);

        User user = new User(ctx.channel(), msg.sender(), msg.recipient());
        newUkcp.user(user);
        channelManager.addKcp(newUkcp);

        iMessageExecutor.execute(() -> {
            try {
                newUkcp.getKcpListener().onConnected(newUkcp);
            } catch (Throwable throwable) {
                newUkcp.getKcpListener().handleException(throwable, newUkcp);
            }
        });

        newUkcp.read(readByteBuf);

        ScheduleTask scheduleTask = new ScheduleTask(iMessageExecutor, newUkcp, hashedWheelTimer);
        hashedWheelTimer.newTimeout(scheduleTask, newUkcp.getInterval(), TimeUnit.MILLISECONDS);
    }

    private int getSn(ByteBuf byteBuf, ChannelConfig channelConfig) {
        int headerSize = 0;
        if (channelConfig.getFecAdapt() != null) {
            headerSize += Fec.fecHeaderSizePlus2;
        }
        return byteBuf.getIntLE(byteBuf.readerIndex() + Kcp.IKCP_SN_OFFSET + headerSize);
    }

}
