package com.hjcenry.ping;

import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 测试多连接吞吐量
 * Created by JinMiao
 * 2019-06-27.
 */
public class KtucpMultiplePingPongExampleClient implements KtucpListener {

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 0, true);
        channelConfig.setSndWnd(256);
        channelConfig.setRcvWnd(256);
        channelConfig.setMtu(400);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        //channelConfig.setAckNoDelay(true);

        //channelConfig.setCrc32Check(true);
        //channelConfig.setTimeoutMillis(10000);

        int clientNumber = 1000;
        for (int i = 0; i < clientNumber; i++) {
            channelConfig.setConv(i);
            KtucpClient ktucpClient = new KtucpClient();
            KtucpMultiplePingPongExampleClient kcpMultiplePingPongExampleClient = new KtucpMultiplePingPongExampleClient();
            ktucpClient.init(kcpMultiplePingPongExampleClient, channelConfig, new InetSocketAddress("127.0.0.1", 10011));
            ktucpClient.connect();
        }
    }

    ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println(uktucp.getConv());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(1004);
                byteBuf.writeInt(1);
                byte[] bytes = new byte[1000];
                byteBuf.writeBytes(bytes);
                uktucp.write(byteBuf);
//                byteBuf.release();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleReceive(Object object, Uktucp uktucp) {
        //System.out.println("收到消息");
        //ukcp.writeMessage(byteBuf);
        //int id = byteBuf.getInt(0);
        //if(j-id%10!=0){
        //    System.out.println("id"+id +"  j" +j);
        //}
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
        System.out.println("连接断开了" + kcp.getConv());
    }


}
