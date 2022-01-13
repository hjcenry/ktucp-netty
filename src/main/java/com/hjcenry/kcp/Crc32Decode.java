package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by JinMiao
 * 2019/12/10.
 */
public class Crc32Decode extends ChannelInboundHandlerAdapter {

    protected CRC32 crc32 = new CRC32();

    protected boolean decode(ByteBuf data) {
        if (!data.isReadable(4)) {
            return false;
        }
        long checksum = data.readUnsignedIntLE();
        ByteBuffer byteBuffer = data.nioBuffer(data.readerIndex(), data.readableBytes());
        crc32.reset();
        crc32.update(byteBuffer);
        if (checksum != crc32.getValue()) {
            Snmp.snmp.getInCsumErrors().increment();
            return false;
        }
        return true;
    }
}
