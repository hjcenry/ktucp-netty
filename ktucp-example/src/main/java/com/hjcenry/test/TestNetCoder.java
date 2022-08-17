package com.hjcenry.test;

import com.google.protobuf.GeneratedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * @ClassName TestKcpCoder
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 12:11
 * @Version 1.0
 **/
public class TestNetCoder {

    public static int encode(int code, GeneratedMessage msg, ByteBuf byteBuf) {
        byte[] bytes = msg == null ? null : msg.toByteArray();
        int len = bytes == null ? 0 : bytes.length;

        byteBuf.writeInt(len);
        byteBuf.writeInt(code);
        if (bytes != null) {
            byteBuf.writeBytes(bytes);
        }
        // 返回长度
        return len;
    }

    public static byte[] decode(ByteBuf in) {
        if (in.isReadable(8)) {
            ByteBuf buffer = null;

            try {
                in.markReaderIndex();
                int len = in.readInt();
                int code = in.readInt();

                if (len > 0 && !in.isReadable(len)) {
                    in.resetReaderIndex();
                    return null;
                }

                if (len < 0) {
                    return null;
                }

                buffer = PooledByteBufAllocator.DEFAULT.heapBuffer(len);
                in.readBytes(buffer);
                byte[] bodys = new byte[len];
                buffer.readBytes(bodys);
                return bodys;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (buffer != null) {
                    buffer.release();
                }
            }
        }
        return null;
    }
}
