package com.hjcenry.net.udp;

import com.hjcenry.kcp.Crc32Encode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

/**
 * UDP编码
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/12 18:51
 **/
public class UdpCrc32Encode extends Crc32Encode {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf data = datagramPacket.content();
        this.encode(data);
        ctx.write(datagramPacket, promise);
    }
}
