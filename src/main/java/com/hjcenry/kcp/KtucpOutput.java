package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface KtucpOutput {

    void out(ByteBuf data, IKcp kcp);

}
