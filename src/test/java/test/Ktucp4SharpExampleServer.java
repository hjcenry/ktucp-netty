package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import com.hjcenry.net.server.KtucpServer;
import io.netty.buffer.ByteBuf;

/**
 * 与c#版本兼容的客户端
 * Created by JinMiao
 * 2019-07-23.
 */
public class Ktucp4SharpExampleServer extends SimpleKtucpListener<ByteBuf> {

    public static void main(String[] args) {

        Ktucp4SharpExampleServer kcpRttExampleServer = new Ktucp4SharpExampleServer();

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
        KtucpServer ktucpServer = new KtucpServer();
        ktucpServer.init(kcpRttExampleServer, channelConfig);
    }


    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + uktucp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }


    @Override
    protected void handleReceive0(ByteBuf buf, Uktucp uktucp) throws Exception {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        System.out.println("收到消息: " + new String(bytes));
        uktucp.write(buf);
    }

    @Override
    public void handleException(Throwable ex, Uktucp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleTimeout!!!:" + uktucp);
    }

    @Override
    public void handleClose(Uktucp kcp) {
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
    }
}
