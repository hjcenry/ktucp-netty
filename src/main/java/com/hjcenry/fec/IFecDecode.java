package com.hjcenry.fec;

import com.hjcenry.fec.fec.FecPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * Created by JinMiao
 * 2021/2/2.
 */
public interface IFecDecode {

    List<ByteBuf> decode(final FecPacket pkt);

    void release();
}
