package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.net.server.KcpServer;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.SimpleKcpListener;
import io.netty.buffer.ByteBuf;

/**
 * 重连测试服务器
 * Created by JinMiao
 * 2019-06-27.
 */
public class KcpReconnectExampleServer extends SimpleKcpListener<ByteBuf> {

    public static void main(String[] args) {
        KcpReconnectExampleServer kcpRttExampleServer = new KcpReconnectExampleServer();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        //channelConfig.setAckNoDelay(true);
        //channelConfig.setCrc32Check(true);
        channelConfig.setUseConvChannel(true);
        channelConfig.setTimeoutMillis(10000);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(kcpRttExampleServer, channelConfig, 10021);
    }


    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }

    int i = 0;

    long start = System.currentTimeMillis();

    @Override
    protected void handleReceive0(ByteBuf cast, Ukcp ukcp) throws Exception {
        i++;
        long now = System.currentTimeMillis();
        if (now - start > 1000) {
            System.out.println("收到消息 time: " + (now - start) + "  message :" + i);
            start = now;
            i = 0;
        }
        ukcp.write(cast);
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