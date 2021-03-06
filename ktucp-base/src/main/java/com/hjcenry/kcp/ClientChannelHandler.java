package com.hjcenry.kcp;

import com.hjcenry.log.KtucpLog;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;

/**
 * Created by JinMiao
 * 2019-06-26.
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    static final Logger logger = KtucpLog.logger;

    private IChannelManager channelManager;

    public ClientChannelHandler(IChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (logger.isErrorEnabled()) {
            logger.error("", cause);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        DatagramPacket msg = (DatagramPacket) object;
        ByteBuf readByteBuf = msg.content();
        Uktucp uktucp = this.channelManager.getKcp(readByteBuf, msg.recipient());
        if (uktucp != null) {
            uktucp.read(readByteBuf);
        }
    }
}
