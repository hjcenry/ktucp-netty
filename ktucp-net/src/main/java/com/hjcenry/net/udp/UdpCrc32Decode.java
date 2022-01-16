package com.hjcenry.net.udp;

import com.hjcenry.kcp.Crc32Decode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * UDP解码
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/12 18:50
 **/
public class UdpCrc32Decode extends Crc32Decode {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof DatagramPacket) {
            DatagramPacket datagramPacket = (DatagramPacket) msg;
            ByteBuf data = datagramPacket.content();
            if (!this.decode(data)) {
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }
}
