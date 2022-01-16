package com.hjcenry.codec.decode;

import com.hjcenry.kcp.Uktucp;
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
    public Object decode(Uktucp uktucp, ByteBuf readByteBuf) {
        return this.decodeByteBuf(uktucp, readByteBuf);
    }

    /**
     * byteBuf消息解码
     *
     * @param uktucp        KCP对象
     * @param readByteBuf 读取byteBuf消息
     * @return 解码对象
     */
    protected abstract I decodeByteBuf(Uktucp uktucp, ByteBuf readByteBuf);
}
