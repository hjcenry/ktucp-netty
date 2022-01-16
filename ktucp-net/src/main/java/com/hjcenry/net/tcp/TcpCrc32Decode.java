package com.hjcenry.net.tcp;

import com.hjcenry.kcp.Crc32Decode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * TCP解码
 *
 * @author hejincheng
 * @date 2022/1/12 18:49
 * @version 1.0
 **/
public class TcpCrc32Decode extends Crc32Decode {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            if (!this.decode(data)) {
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }
}
