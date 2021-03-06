package com.hjcenry.rtt;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2019-06-26.
 */
public class KtucpRttExampleClient extends SimpleKtucpListener<ByteBuf> {

    private final ByteBuf data;

    private int[] rtts;

    private volatile int count;

    private ScheduledExecutorService scheduleSrv;

    private ScheduledFuture<?> future = null;

    private final long startTime;

    public KtucpRttExampleClient() {
        data = Unpooled.buffer(200);
        for (int i = 0; i < data.capacity(); i++) {
            data.writeByte((byte) i);
        }

        rtts = new int[300];
        for (int i = 0; i < rtts.length; i++) {
            rtts[i] = -1;
        }
        startTime = System.currentTimeMillis();
        scheduleSrv = new ScheduledThreadPoolExecutor(1);
    }

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(512);
        channelConfig.setRcvWnd(512);
        channelConfig.setMtu(512);
        channelConfig.setAckNoDelay(true);
        channelConfig.setConv(55);

        channelConfig.setFecAdapt(new FecAdapt(3, 1));
        channelConfig.setCrc32Check(true);
        channelConfig.setTimeoutMillis(10000);
        //channelConfig.setAckMaskSize(32);
        KtucpClient ktucpClient = new KtucpClient();
        KtucpRttExampleClient kcpClientRttExample = new KtucpRttExampleClient();
        ktucpClient.init(kcpClientRttExample, channelConfig, new InetSocketAddress("127.0.0.1", 20003));
        ktucpClient.connect();
    }

    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        future = scheduleSrv.scheduleWithFixedDelay(() -> {
            ByteBuf byteBuf = rttMsg(++count);
            uktucp.write(byteBuf);
            if (count >= rtts.length) {
                // finish
                future.cancel(true);
                byteBuf = rttMsg(-1);
                uktucp.write(byteBuf);
            }
        }, 20, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void handleReceive0(ByteBuf cast, Uktucp uktucp) throws Exception {
        int curCount = cast.readShort();

        if (curCount == -1) {
            scheduleSrv.schedule(new Runnable() {
                @Override
                public void run() {
                    int sum = 0;
                    for (int rtt : rtts) {
                        sum += rtt;
                    }
                    System.out.println("average: " + (sum / rtts.length));
                    System.out.println(Snmp.snmp.toString());
                    uktucp.close();
                    //ukcp.setTimeoutMillis(System.currentTimeMillis());
                    System.exit(0);
                }
            }, 3, TimeUnit.SECONDS);
        } else {
            int idx = curCount - 1;
            long time = cast.readInt();
            if (rtts[idx] != -1) {
                System.out.println("???");
            }
            //log.info("rcv count {} {}", curCount, System.currentTimeMillis());
            rtts[idx] = (int) (System.currentTimeMillis() - startTime - time);
            System.out.println("rtt : " + curCount + "  " + rtts[idx]);
        }
    }

    @Override
    public void handleException(Throwable ex, Uktucp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Uktucp kcp) {
        scheduleSrv.shutdown();
        try {
            scheduleSrv.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int sum = 0;
        int max = 0;
        for (int rtt : rtts) {
            if (rtt > max) {
                max = rtt;
            }
            sum += rtt;
        }
        System.out.println("average: " + (sum / rtts.length) + " max:" + max);
        System.out.println(Snmp.snmp.toString());
        System.out.println("lost percent: " + (Snmp.snmp.getRetransmitSegments().doubleValue() / Snmp.snmp.getOutPackets().doubleValue()));


    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleTimeout!!!:" + uktucp);
    }

    /**
     * count+timestamp+dataLen+data
     *
     * @param count
     * @return
     */
    public ByteBuf rttMsg(int count) {
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeShort(count);
        buf.writeInt((int) (System.currentTimeMillis() - startTime));

        //int dataLen = new Random().nextInt(200);
        //buf.writeBytes(new byte[dataLen]);

        int dataLen = data.readableBytes();
        buf.writeShort(dataLen);
        buf.writeBytes(data, data.readerIndex(), dataLen);

        return buf;
    }

}
