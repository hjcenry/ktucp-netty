package com.hjcenry.codec.encode;

import com.hjcenry.kcp.Ukcp;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.EncoderException;
import io.netty.util.internal.TypeParameterMatcher;

/**
 * 抽象消息编码
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 14:17
 **/
public abstract class MessageToByteEncoder<I> implements IMessageEncoder {

    private final TypeParameterMatcher matcher;
    private final boolean preferDirect;
    private final ByteBufAllocator byteBufAllocator;

    public MessageToByteEncoder() {
        this(ByteBufAllocator.DEFAULT, true);
    }

    public MessageToByteEncoder(ByteBufAllocator byteBufAllocator) {
        this(byteBufAllocator, true);
    }

    public MessageToByteEncoder(ByteBufAllocator byteBufAllocator, boolean preferDirect) {
        this.matcher = TypeParameterMatcher.find(this, MessageToByteEncoder.class, "I");
        this.byteBufAllocator = byteBufAllocator;
        this.preferDirect = preferDirect;
    }

    @Override
    public ByteBuf encode(Ukcp ukcp, Object writeObject) {
        ByteBuf buf;
        try {
            if (!acceptOutboundMessage(writeObject)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            I cast = (I) writeObject;
            buf = allocateBuffer(byteBufAllocator, cast, preferDirect);
            try {
                encodeObject(ukcp, cast, buf);
            } finally {
                ReferenceCountUtil.release(cast);
            }
            return buf;
        } catch (EncoderException e) {
            throw e;
        } catch (Throwable e) {
            throw new EncoderException(e);
        }
    }

    /**
     * byteBuf消息编码
     *
     * @param ukcp
     * @param writeObject   写对象
     * @param targetByteBuf 目标byteBuf
     * @return 编码对象
     */
    protected abstract void encodeObject(Ukcp ukcp, I writeObject, ByteBuf targetByteBuf);

    /**
     * Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
     * {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }

    /**
     * Allocate a {@link ByteBuf} which will be used as argument of {@link #encodeObject(Ukcp, Object, ByteBuf)}.
     * Sub-classes may override this method to return {@link ByteBuf} with a perfect matching {@code initialCapacity}.
     */
    protected ByteBuf allocateBuffer(ByteBufAllocator byteBufAllocator, @SuppressWarnings("unused") I msg, boolean preferDirect) {
        if (preferDirect) {
            return byteBufAllocator.ioBuffer();
        } else {
            return byteBufAllocator.heapBuffer();
        }
    }
}
