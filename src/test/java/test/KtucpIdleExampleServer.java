package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.server.KtucpServer;
import com.hjcenry.kcp.Ukcp;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试大量连接不通讯的例子
 * Created by JinMiao
 * 2019-07-10.
 */
public class KtucpIdleExampleServer implements KtucpListener {

    public static void main(String[] args) {

        KtucpIdleExampleServer kcpIdleExampleServer = new KtucpIdleExampleServer();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        channelConfig.setAckNoDelay(false);
        channelConfig.setCrc32Check(true);
        //channelConfig.setTimeoutMillis(10000);
        KtucpServer ktucpServer = new KtucpServer();
        ktucpServer.init(kcpIdleExampleServer, channelConfig, 10020);
    }

    private AtomicInteger atomicInteger = new AtomicInteger();

    private AtomicInteger recieveAtomicInteger = new AtomicInteger();


    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        int id = atomicInteger.incrementAndGet();
        ukcp.user().setCache(id);

        System.out.println("有连接进来,当前连接" + id);
    }

    int i = 0;

    long start = System.currentTimeMillis();

    @Override
    public void handleReceive(Object object, Ukcp kcp) {
        System.out.println("收到消息 " + recieveAtomicInteger.incrementAndGet());
        i++;
        long now = System.currentTimeMillis();
        if (now - start > 1000) {
            System.out.println("收到消息 time: " + (now - start) + "  message :" + i);
            start = now;
            i = 0;
        }
        //kcp.write(buf);
    }

    @Override
    public void handleIdleTimeout(Ukcp ukcp) {
        System.out.println("handleTimeout!!!:" + ukcp);
    }

    @Override
    public void handleException(Throwable ex, Ukcp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Ukcp kcp) {
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
        System.out.println("连接断开了,当前连接" + atomicInteger.decrementAndGet());
    }
}
