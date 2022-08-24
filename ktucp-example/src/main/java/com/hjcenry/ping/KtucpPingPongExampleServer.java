package com.hjcenry.ping;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.net.server.KtucpServer;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.DefaultEventLoop;

/**
 * 测试单连接吞吐量
 * mbp 2.3 GHz Intel Core i9 16GRam 单连接 带fec 5W/s qps 单连接 不带fec 8W/s qps
 * Created by JinMiao
 * 2019-06-27.
 */
public class KtucpPingPongExampleServer extends SimpleKtucpListener<ByteBuf> {
    static DefaultEventLoop logicThread = new DefaultEventLoop();

    public static void main(String[] args) {

        KtucpPingPongExampleServer kcpRttExampleServer = new KtucpPingPongExampleServer();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        //channelConfig.setiMessageExecutorPool(new DisruptorExecutorPool(Runtime.getRuntime().availableProcessors()));
        //channelConfig.setFecAdapt(new FecAdapt(10,3));
        channelConfig.setAckNoDelay(false);
        //channelConfig.setCrc32Check(true);
        channelConfig.setTimeoutMillis(10000);
        KtucpServer ktucpServer = new KtucpServer();
        ktucpServer.init(kcpRttExampleServer, channelConfig, 10001);
    }


    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + uktucp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }

    int i = 0;

    long start = System.currentTimeMillis();

    @Override
    protected void handleReceive0(ByteBuf cast, Uktucp uktucp) throws Exception {
        ByteBuf newBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        logicThread.execute(() -> {
            try {
                i++;
                long now = System.currentTimeMillis();
                if (now - start > 1000) {
                    System.out.println("收到消息 time: " + (now - start) + "  message :" + i);
                    start = now;
                    i = 0;
                }
                uktucp.write(newBuf);
                // WriteTask 70行会自动进行释放，因此ByteBuf无需自行释放
//                newBuf.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
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
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
        System.out.println("连接断开了");
    }
}