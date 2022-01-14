package com.hjcenry.kcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.internal.CodecOutputList;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.threadPool.ITask;
import com.hjcenry.time.IKtucpTimeService;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;

import java.util.Queue;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public class ReadTask implements ITask {

    private final Uktucp uktucp;
    private final IMessageDecoder messageDecoder;
    private final IKtucpTimeService kcpTimeService;

    public ReadTask(Uktucp uktucp, IMessageDecoder messageDecoder) {
        this.uktucp = uktucp;
        this.kcpTimeService = uktucp.getKcpTimeService();
        this.messageDecoder = messageDecoder;
    }

    @Override
    public void execute() {
        CodecOutputList<ByteBuf> bufList = null;
        Uktucp uktucp = this.uktucp;
        try {
            //查看连接状态
            if (!uktucp.isActive()) {
                return;
            }
            long current = this.kcpTimeService.now();
            Queue<ByteBuf> receiveList = uktucp.getReadBufferQueue();
            int readCount = 0;
            for (; ; ) {
                ByteBuf byteBuf = receiveList.poll();
                if (byteBuf == null) {
                    break;
                }
                readCount++;
                uktucp.input(byteBuf, current);
                byteBuf.release();
            }
            if (readCount == 0) {
                return;
            }
            if (uktucp.isControlReadBufferSize()) {
                uktucp.getReadBufferIncr().addAndGet(readCount);
            }
            long readBytes = 0;
            if (uktucp.isStream()) {
                int size = 0;
                while (uktucp.canRecv()) {
                    if (bufList == null) {
                        bufList = CodecOutputList.newInstance();
                    }
                    uktucp.receive(bufList);
                    size = bufList.size();
                }
                for (int i = 0; i < size; i++) {
                    ByteBuf byteBuf = bufList.getUnsafe(i);
                    readBytes += byteBuf.readableBytes();
                    readByteBuf(byteBuf, current, uktucp);
                }
            } else {
                while (uktucp.canRecv()) {
                    ByteBuf recvBuf = uktucp.mergeReceive();
                    readBytes += recvBuf.readableBytes();
                    readByteBuf(recvBuf, current, uktucp);
                }
            }
            Snmp.snmp.BytesReceived.add(readBytes);
            //判断写事件
            if (!uktucp.getWriteObjectQueue().isEmpty() && uktucp.canSend(false)) {
                uktucp.notifyWriteEvent();
            }
        } catch (Exception e) {
            uktucp.getKcpListener().handleException(e, uktucp);
            uktucp.internalClose();
            e.printStackTrace();
        } finally {
            release();
            if (bufList != null) {
                bufList.recycle();
            }
        }
    }

    private void readByteBuf(ByteBuf buf, long current, Uktucp uktucp) {
        uktucp.setLastReceiveTime(current);
        Object object = null;
        try {
            // 消息解码
            if (this.messageDecoder == null) {
                object = buf;
            } else {
                object = this.messageDecoder.decode(uktucp, buf);
            }
            KtucpListener ktucpListener = uktucp.getKcpListener();
            // 处理读事件
            ktucpListener.handleReceive(object, uktucp);
        } catch (Throwable throwable) {
            uktucp.getKcpListener().handleException(throwable, uktucp);
        } finally {
            ReferenceCountUtil.release(buf);
            ReferenceCountUtil.release(object);
        }
    }

    public void release() {
        uktucp.getReadProcessing().set(false);
    }

}
