package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.SimpleKcpListener;
import com.hjcenry.net.server.KcpServer;
import io.netty.buffer.ByteBuf;

/**
 * 与c#版本兼容的客户端
 * Created by JinMiao
 * 2019-07-23.
 */
public class Kcp4sharpExampleServer extends SimpleKcpListener<ByteBuf> {

    public static void main(String[] args) {

        Kcp4sharpExampleServer kcpRttExampleServer = new Kcp4sharpExampleServer();

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 10, 2, true);
        channelConfig.setSndWnd(300);
        channelConfig.setRcvWnd(300);
        channelConfig.setMtu(512);
        channelConfig.setAckNoDelay(true);
        channelConfig.setTimeoutMillis(10000);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        //c# crc32未实现
        channelConfig.setCrc32Check(false);
//        KcpServer kcpServer = new KcpServer();
//        kcpServer.init(kcpRttExampleServer, channelConfig, 10009);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(kcpRttExampleServer, channelConfig);
    }


    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getRemoteAddress());
    }


    @Override
    protected void handleReceive0(ByteBuf buf, Ukcp ukcp) throws Exception {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        System.out.println("收到消息: " + new String(bytes));
        ukcp.write(buf);
    }

    @Override
    public void handleException(Throwable ex, Ukcp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleIdleTimeout(Ukcp ukcp) {
        System.out.println("handleTimeout!!!:" + ukcp);
    }

    @Override
    public void handleClose(Ukcp kcp) {
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
    }
}
