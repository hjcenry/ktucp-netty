package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface KtucpOutput {

    /**
     * 输出方法
     *
     * @param data 数据
     * @param kcp  KCP对象
     */
    void out(ByteBuf data, IKcp kcp);

}
