package com.hjcenry.kcp;

import com.hjcenry.coder.IMessageEncoder;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.threadPool.ITask;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.Queue;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public class WriteTask implements ITask {

    private final Ukcp ukcp;
    private final IMessageEncoder messageEncoder;

    public WriteTask(Ukcp ukcp, IMessageEncoder messageEncoder) {
        this.ukcp = ukcp;
        this.messageEncoder = messageEncoder;
    }

    @Override
    public void execute() {
        Ukcp ukcp = this.ukcp;
        try {
            //查看连接状态
            if (!ukcp.isActive()) {
                return;
            }
            //从发送缓冲区到kcp缓冲区
            Queue<Object> queue = ukcp.getWriteObjectQueue();
            int writeCount = 0;
            long writeBytes = 0;
            while (ukcp.canSend(false)) {
                Object object = queue.poll();
                if (object == null) {
                    break;
                }
                ByteBuf byteBuf = null;
                try {
                    // 消息编码
                    byteBuf = this.messageEncoder.encode(object);
                    if (byteBuf == null) {
                        break;
                    }
                    writeCount++;
                    writeBytes += byteBuf.readableBytes();
                    // 发送
                    ukcp.send(byteBuf);
                } catch (IOException e) {
                    ukcp.getKcpListener().handleException(e, ukcp);
                    return;
                } finally {
                    // release
                    if (byteBuf != null) {
                        byteBuf.release();
                    }
                    ReferenceCountUtil.release(object);
                }
            }
            Snmp.snmp.BytesSent.add(writeBytes);
            if (ukcp.isControlWriteBufferSize()) {
                ukcp.getWriteBufferIncr().addAndGet(writeCount);
            }
            //如果有发送 则检测时间
            if (!ukcp.canSend(false) || (ukcp.checkFlush() && ukcp.isFastFlush())) {
                long now = System.currentTimeMillis();
                long next = ukcp.flush(now);
                ukcp.setTsUpdate(now + next);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    public void release() {
        ukcp.getWriteProcessing().set(false);
    }
}
