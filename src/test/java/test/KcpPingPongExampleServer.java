package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.net.server.KcpServer;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.SimpleKcpListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.DefaultEventLoop;

/**
 * 测试单连接吞吐量
 * mbp 2.3 GHz Intel Core i9 16GRam 单连接 带fec 5W/s qps 单连接 不带fec 8W/s qps
 * Created by JinMiao
 * 2019-06-27.
 */
public class KcpPingPongExampleServer extends SimpleKcpListener<ByteBuf> {
    static DefaultEventLoop logicThread = new DefaultEventLoop();

    public static void main(String[] args) {

        KcpPingPongExampleServer kcpRttExampleServer = new KcpPingPongExampleServer();
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
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(kcpRttExampleServer, channelConfig, 10001);
    }


    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }

    int i = 0;

    long start = System.currentTimeMillis();

    @Override
    protected void handleReceive0(ByteBuf cast, Ukcp ukcp) throws Exception {
        ByteBuf newBuf = cast.retainedDuplicate();
        logicThread.execute(() -> {
            try {
                i++;
                long now = System.currentTimeMillis();
                if (now - start > 1000) {
                    System.out.println("收到消息 time: " + (now - start) + "  message :" + i);
                    start = now;
                    i = 0;
                }
                ukcp.write(newBuf);
                newBuf.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
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
        System.out.println("连接断开了");
    }
}