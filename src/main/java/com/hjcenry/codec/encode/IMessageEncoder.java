package com.hjcenry.codec.encode;

import com.hjcenry.codec.IMessageCoder;
import io.netty.buffer.ByteBuf;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 14:54
 **/
public interface IMessageEncoder extends IMessageCoder {

    /**
     * byteBuf消息编码
     * <p>
     * <b>调用需要处理返回的ByteBuf的release</b><br/>
     * invoke {@link io.netty.util.ReferenceCountUtil#release(Object)}
     * </p>
     *
     * @param writeObject 写对象
     * @return 编码对象
     */
    public ByteBuf encode(Object writeObject);
}
