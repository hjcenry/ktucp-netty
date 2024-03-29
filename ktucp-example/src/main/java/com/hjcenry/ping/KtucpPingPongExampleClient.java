package com.hjcenry.ping;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.DefaultEventLoop;

import java.net.InetSocketAddress;

/**
 * 测试单连接吞吐量
 * Created by JinMiao
 * 2019-06-27.
 */
public class KtucpPingPongExampleClient extends SimpleKtucpListener<ByteBuf> {

    static DefaultEventLoop logicThread = new DefaultEventLoop();

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        //channelConfig.setiMessageExecutorPool(new DisruptorExecutorPool(Runtime.getRuntime().availableProcessors()));
        //channelConfig.setFecAdapt(new FecAdapt(10,3));
        channelConfig.setAckNoDelay(false);
        //channelConfig.setCrc32Check(true);
        //channelConfig.setTimeoutMillis(10000);

        KtucpClient ktucpClient = new KtucpClient();
        KtucpPingPongExampleClient kcpClientRttExample = new KtucpPingPongExampleClient();
        ktucpClient.init(kcpClientRttExample, channelConfig,new InetSocketAddress("127.0.0.1", 10001));

        ktucpClient.connect();
    }

    int i = 0;

    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        for (int i = 0; i < 100; i++) {
            ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(1024);
            byteBuf.writeInt(i++);
            byte[] bytes = new byte[1020];
            byteBuf.writeBytes(bytes);
            uktucp.write(byteBuf);
            // WriteTask 70行会自动进行释放，因此ByteBuf无需自行释放
//            byteBuf.release();
        }
    }

    int j = 0;


    @Override
    protected void handleReceive0(ByteBuf cast, Uktucp uktucp) throws Exception {
        ByteBuf newBuf = cast.retainedDuplicate();
        logicThread.execute(() -> {
            try {
                uktucp.write(newBuf);
                // WriteTask 70行会自动进行释放，因此ByteBuf无需自行释放
//                newBuf.release();
                j++;
                if (j % 100000 == 0) {
                    System.out.println(Snmp.snmp.toString());
                    System.out.println("收到了 返回回去" + j);
                }
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
        System.out.println("连接断开了");
    }


}
