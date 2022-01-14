package com.hjcenry.codec.decode;

import com.hjcenry.codec.IMessageCoder;
import com.hjcenry.kcp.Uktucp;
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
     * @param uktucp        KCP对象
     * @param readByteBuf 读取byteBuf消息
     * @return 解码对象
     */
    public Object decode(Uktucp uktucp, ByteBuf readByteBuf);
}
