package com.hjcenry.codec.decode;

import com.hjcenry.kcp.Ukcp;
import io.netty.buffer.ByteBuf;

/**
 * 抽象消息解码
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 14:17
 **/
public abstract class ByteToMessageDecoder<I> implements IMessageDecoder {

    @Override
    public Object decode(Ukcp ukcp, ByteBuf readByteBuf) {
        return this.decodeByteBuf(ukcp, readByteBuf);
    }

    /**
     * byteBuf消息解码
     *
     * @param ukcp        KCP对象
     * @param readByteBuf 读取byteBuf消息
     * @return 解码对象
     */
    protected abstract I decodeByteBuf(Ukcp ukcp, ByteBuf readByteBuf);
}
