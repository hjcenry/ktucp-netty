package com.hjcenry.kcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.internal.CodecOutputList;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.threadPool.ITask;
import com.hjcenry.time.IKcpTimeService;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;

import java.util.Queue;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public class ReadTask implements ITask {

    private final Ukcp ukcp;
    private final IMessageDecoder messageDecoder;
    private final IKcpTimeService kcpTimeService;

    public ReadTask(Ukcp ukcp, IMessageDecoder messageDecoder) {
        this.ukcp = ukcp;
        this.kcpTimeService = ukcp.getKcpTimeService();
        this.messageDecoder = messageDecoder;
    }

    @Override
    public void execute() {
        CodecOutputList<ByteBuf> bufList = null;
        Ukcp ukcp = this.ukcp;
        try {
            //查看连接状态
            if (!ukcp.isActive()) {
                return;
            }
            long current = this.kcpTimeService.now();
            Queue<ByteBuf> receiveList = ukcp.getReadBufferQueue();
            int readCount = 0;
            for (; ; ) {
                ByteBuf byteBuf = receiveList.poll();
                if (byteBuf == null) {
                    break;
                }
                readCount++;
                ukcp.input(byteBuf, current);
                byteBuf.release();
            }
            if (readCount == 0) {
                return;
            }
            if (ukcp.isControlReadBufferSize()) {
                ukcp.getReadBufferIncr().addAndGet(readCount);
            }
            long readBytes = 0;
            if (ukcp.isStream()) {
                int size = 0;
                while (ukcp.canRecv()) {
                    if (bufList == null) {
                        bufList = CodecOutputList.newInstance();
                    }
                    ukcp.receive(bufList);
                    size = bufList.size();
                }
                for (int i = 0; i < size; i++) {
                    ByteBuf byteBuf = bufList.getUnsafe(i);
                    readBytes += byteBuf.readableBytes();
                    readByteBuf(byteBuf, current, ukcp);
                }
            } else {
                while (ukcp.canRecv()) {
                    ByteBuf recvBuf = ukcp.mergeReceive();
                    readBytes += recvBuf.readableBytes();
                    readByteBuf(recvBuf, current, ukcp);
                }
            }
            Snmp.snmp.BytesReceived.add(readBytes);
            //判断写事件
            if (!ukcp.getWriteObjectQueue().isEmpty() && ukcp.canSend(false)) {
                ukcp.notifyWriteEvent();
            }
        } catch (Exception e) {
            ukcp.getKcpListener().handleException(e, ukcp);
            ukcp.internalClose();
            e.printStackTrace();
        } finally {
            release();
            if (bufList != null) {
                bufList.recycle();
            }
        }
    }

    private void readByteBuf(ByteBuf buf, long current, Ukcp ukcp) {
        ukcp.setLastReceiveTime(current);
        Object object = null;
        try {
            // 消息解码
            if (this.messageDecoder == null) {
                object = buf;
            } else {
                object = this.messageDecoder.decode(ukcp, buf);
            }
            KtucpListener ktucpListener = ukcp.getKcpListener();
            // 处理读事件
            ktucpListener.handleReceive(object, ukcp);
        } catch (Throwable throwable) {
            ukcp.getKcpListener().handleException(throwable, ukcp);
        } finally {
            ReferenceCountUtil.release(buf);
            ReferenceCountUtil.release(object);
        }
    }

    public void release() {
        ukcp.getReadProcessing().set(false);
    }

}
