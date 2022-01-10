package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:58
 **/
public abstract class AbstractChannelManager implements IChannelManager {

    protected int convIndex;

    @Override
    public int getConvIdByByteBuf(ByteBuf readByteBuf) {
        return readByteBuf.getIntLE(readByteBuf.readerIndex() + this.convIndex);
    }
}
