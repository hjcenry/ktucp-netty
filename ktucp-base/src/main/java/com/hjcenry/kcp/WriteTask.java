package com.hjcenry.kcp;

import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.threadpool.ITask;
import com.hjcenry.time.IKtucpTimeService;
import com.hjcenry.util.ReferenceCountUtil;
import com.hjcenry.fec.fec.Snmp;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

import java.util.Queue;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public class WriteTask implements ITask {

    protected static final Logger logger = KtucpLog.logger;

    private final Uktucp uktucp;
    private final IMessageEncoder messageEncoder;
    private final IKtucpTimeService kcpTimeService;

    public WriteTask(Uktucp uktucp, IMessageEncoder messageEncoder) {
        this.uktucp = uktucp;
        this.kcpTimeService = uktucp.getKcpTimeService();
        this.messageEncoder = messageEncoder;
    }

    @Override
    public void execute() {
        Uktucp uktucp = this.uktucp;
        try {
            //查看连接状态
            if (!uktucp.isActive()) {
                return;
            }
            //从发送缓冲区到kcp缓冲区
            Queue<Object> queue = uktucp.getWriteObjectQueue();
            int writeCount = 0;
            long writeBytes = 0;
            while (uktucp.canSend(false)) {
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
                        byteBuf = this.messageEncoder.encode(uktucp, object);
                    }

                    if (byteBuf == null) {
                        break;
                    }
                    writeCount++;
                    writeBytes += byteBuf.readableBytes();
                    // 发送
                    uktucp.send(byteBuf);
                } catch (Exception e) {
                    uktucp.getKcpListener().handleException(e, uktucp);
                    return;
                } finally {
                    // release
                    ReferenceCountUtil.release(byteBuf);
                }
            }

            // 统计
            Snmp.snmp.addBytesSent(writeBytes);
            uktucp.getSnmp().addBytesSent(writeBytes);

            if (uktucp.isControlWriteBufferSize()) {
                uktucp.getWriteBufferIncr().addAndGet(writeCount);
            }
            //如果有发送 则检测时间
            if (!uktucp.canSend(false) || (uktucp.checkFlush() && uktucp.isFastFlush())) {
                long now = this.kcpTimeService.now();
                long next = uktucp.flush(now);
                uktucp.setTsUpdate(now + next);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            uktucp.getKcpListener().handleException(e, uktucp);
        } finally {
            release();
        }
    }

    public void release() {
        uktucp.getWriteProcessing().set(false);
    }
}
