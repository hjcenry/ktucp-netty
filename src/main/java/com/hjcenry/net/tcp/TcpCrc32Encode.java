package com.hjcenry.net.tcp;

import com.hjcenry.kcp.Crc32Encode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * TCP编码
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/12 18:50
 **/
public class TcpCrc32Encode extends Crc32Encode {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ByteBuf data = (ByteBuf) msg;
        this.encode(data);
        ctx.write(msg, promise);
    }
}
