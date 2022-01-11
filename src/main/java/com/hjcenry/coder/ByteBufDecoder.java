package com.hjcenry.coder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * ByteBuf解码器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 15:05
 **/
public class ByteBufDecoder extends ByteToMessageDecoder<ByteBuf> {

    @Override
    protected ByteBuf decodeByteBuf(ByteBuf readByteBuf) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(readByteBuf.array());
        return byteBuf;
    }
}
