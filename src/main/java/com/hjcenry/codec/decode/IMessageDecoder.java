package com.hjcenry.codec.decode;

import com.hjcenry.codec.IMessageCoder;
import com.hjcenry.kcp.Ukcp;
import io.netty.buffer.ByteBuf;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 14:53
 **/
public interface IMessageDecoder extends IMessageCoder {

    /**
     * byteBuf消息解码
     *
     * @param ukcp        KCP对象
     * @param readByteBuf 读取byteBuf消息
     * @return 解码对象
     */
    public Object decode(Ukcp ukcp, ByteBuf readByteBuf);
}
