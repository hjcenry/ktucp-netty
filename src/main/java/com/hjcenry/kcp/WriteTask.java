package com.hjcenry.kcp;

import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.log.KcpLog;
import com.hjcenry.threadPool.ITask;
import com.hjcenry.time.IKcpTimeService;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

import java.util.Queue;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public class WriteTask implements ITask {

    protected static final Logger logger = KcpLog.logger;

    private final Ukcp ukcp;
    private final IMessageEncoder messageEncoder;
    private final IKcpTimeService kcpTimeService;

    public WriteTask(Ukcp ukcp, IMessageEncoder messageEncoder) {
        this.ukcp = ukcp;
        this.kcpTimeService = ukcp.getKcpTimeService();
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
                    if (this.messageEncoder == null) {
                        byteBuf = (ByteBuf) object;
                    } else {
                        // 消息编码
                        byteBuf = this.messageEncoder.encode(object);
                    }

                    if (byteBuf == null) {
                        break;
                    }
                    writeCount++;
                    writeBytes += byteBuf.readableBytes();
                    // 发送
                    ukcp.send(byteBuf);
                } catch (Exception e) {
                    ukcp.getKcpListener().handleException(e, ukcp);
                    return;
                } finally {
                    // release
                    ReferenceCountUtil.release(byteBuf);
                }
            }
            Snmp.snmp.BytesSent.add(writeBytes);
            if (ukcp.isControlWriteBufferSize()) {
                ukcp.getWriteBufferIncr().addAndGet(writeCount);
            }
            //如果有发送 则检测时间
            if (!ukcp.canSend(false) || (ukcp.checkFlush() && ukcp.isFastFlush())) {
                long now = this.kcpTimeService.now();
                long next = ukcp.flush(now);
                ukcp.setTsUpdate(now + next);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            ukcp.getKcpListener().handleException(e, ukcp);
        } finally {
            release();
        }
    }

    public void release() {
        ukcp.getWriteProcessing().set(false);
    }
}
