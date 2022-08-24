package com.hjcenry.speed;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.client.KtucpClient;
import com.hjcenry.threadpool.disruptor.DisruptorExecutorPool;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * Created by JinMiao
 * 2020/12/23.
 */
public class SpeedExampleClient implements KtucpListener {


    public SpeedExampleClient() {
    }

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 30, 2, true);
        channelConfig.setSndWnd(2048);
        channelConfig.setRcvWnd(2048);
        channelConfig.setMtu(1400);
        channelConfig.setAckNoDelay(true);
        channelConfig.setConv(55);
        channelConfig.setMessageExecutorPool(new DisruptorExecutorPool(Runtime.getRuntime().availableProcessors() / 2));
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        channelConfig.setCrc32Check(false);
        channelConfig.setWriteBufferSize(channelConfig.getMtu() * 300000);
        KtucpClient ktucpClient = new KtucpClient();
        SpeedExampleClient speedExampleClient = new SpeedExampleClient();
        ktucpClient.init(speedExampleClient, channelConfig, new InetSocketAddress("127.0.0.1", 20004));

        ktucpClient.connect();

    }

    private static final int messageSize = 2048;
    private long start = System.currentTimeMillis();

    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        ExecutorService thread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory("Single"));
        thread.submit((Runnable) () -> {
            for (; ; ) {
                long now = System.currentTimeMillis();
                if (now - start >= 1000) {
                    System.out.println("耗时 :" + (now - start) + " 发送数据: " + (Snmp.snmp.getOutBytes().doubleValue() / 1024.0 / 1024.0) + "MB" + " 有效数据: " + Snmp.snmp.getBytesSent().doubleValue() / 1024.0 / 1024.0 + " MB");
                    System.out.println(Snmp.snmp.toString());
                    Snmp.snmp = new Snmp();
                    start = now;
                }
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(messageSize);
                byteBuf.writeBytes(new byte[messageSize]);
                uktucp.write(byteBuf);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // WriteTask 70行会自动进行释放，因此ByteBuf无需自行释放
//                byteBuf.release();
            }
        });
    }

    @Override
    public void handleReceive(Object object, Uktucp uktucp) {
    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleTimeout!!!:" + uktucp);
    }

    @Override
    public void handleException(Throwable ex, Uktucp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Uktucp kcp) {
    }
}
