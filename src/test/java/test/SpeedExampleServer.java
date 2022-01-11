package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.kcp.KcpServer;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.SimpleKcpListener;
import com.hjcenry.threadPool.disruptor.DisruptorExecutorPool;
import io.netty.buffer.ByteBuf;

/**
 * 测试吞吐量
 * Created by JinMiao
 * 2020/12/23.
 */
public class SpeedExampleServer extends SimpleKcpListener<ByteBuf> {
    public static void main(String[] args) {

        SpeedExampleServer speedExampleServer = new SpeedExampleServer();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 30, 2, true);
        channelConfig.setSndWnd(2048);
        channelConfig.setRcvWnd(2048);
        channelConfig.setMtu(1400);
        channelConfig.setIMessageExecutorPool(new DisruptorExecutorPool(Runtime.getRuntime().availableProcessors() / 2));
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        channelConfig.setAckNoDelay(true);
        channelConfig.setTimeoutMillis(5000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setCrc32Check(false);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(speedExampleServer, channelConfig, 20004);
    }

    long start = System.currentTimeMillis();


    @Override
    public void onConnected(Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getRemoteAddress());
    }

    long inBytes = 0;

    @Override
    protected void handleReceive0(ByteBuf cast, Ukcp ukcp) throws Exception {
        inBytes += cast.readableBytes();
        long now = System.currentTimeMillis();
        if (now - start >= 1000) {
            System.out.println("耗时 :" + (now - start) + " 接收数据: " + (Snmp.snmp.InBytes.doubleValue() / 1024.0 / 1024.0) + "MB" + " 有效数据: " + inBytes / 1024.0 / 1024.0 + " MB");
            System.out.println(Snmp.snmp.BytesReceived.doubleValue() / 1024.0 / 1024.0);
            System.out.println(Snmp.snmp.toString());
            inBytes = 0;
            Snmp.snmp = new Snmp();
            start = now;
        }
    }

    @Override
    public void handleException(Throwable ex, Ukcp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Ukcp kcp) {
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
    }
}
