package com.hjcenry.coder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

/**
 * 抽象消息解码
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 14:17
 **/
public abstract class ByteToMessageDecoder<I> implements IMessageDecoder {

    @Override
    public Object decode(ByteBuf readByteBuf) {
        return this.decodeByteBuf(readByteBuf);
    }

    /**
     * byteBuf消息解码
     *
     * @param readByteBuf 读取byteBuf消息
     * @return 解码对象
     */
    protected abstract I decodeByteBuf(ByteBuf readByteBuf);
}
