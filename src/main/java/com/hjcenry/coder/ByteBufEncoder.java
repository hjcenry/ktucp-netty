package com.hjcenry.coder;

import io.netty.buffer.ByteBuf;

/**
 * ByteBuf编码器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 15:03
 **/
public class ByteBufEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encodeObject(ByteBuf writeObject, ByteBuf targetByteBuf) {
        targetByteBuf.writeBytes(writeObject.array());
    }
}
