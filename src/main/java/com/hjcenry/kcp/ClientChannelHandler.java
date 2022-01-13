package com.hjcenry.kcp;

import com.hjcenry.log.KcpLog;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JinMiao
 * 2019-06-26.
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    static final Logger logger = KcpLog.logger;

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
        Ukcp ukcp = this.channelManager.getKcp(readByteBuf, msg.recipient());
        if (ukcp != null) {
            ukcp.read(readByteBuf);
        }
    }
}
