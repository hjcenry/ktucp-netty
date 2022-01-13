package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by JinMiao
 * 2019/12/10.
 */
public class Crc32Encode extends ChannelOutboundHandlerAdapter {

    protected CRC32 crc32 = new CRC32();

    protected void encode(ByteBuf data) {
        ByteBuffer byteBuffer = data.nioBuffer(ChannelConfig.crc32Size, data.readableBytes() - ChannelConfig.crc32Size);
        crc32.reset();
        crc32.update(byteBuffer);
        long checksum = crc32.getValue();
        data.setIntLE(0, (int) checksum);
    }
}
