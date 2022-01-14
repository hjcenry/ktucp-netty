package test;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.SimpleKcpListener;
import com.hjcenry.net.server.KcpServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2018/11/2.
 */
public class KcpRttExampleServer extends SimpleKcpListener<ByteBuf> {

    public static void main(String[] args) {

        KcpRttExampleServer kcpRttExampleServer = new KcpRttExampleServer();

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(512);
        channelConfig.setRcvWnd(512);
        channelConfig.setMtu(512);
        channelConfig.setFecAdapt(new FecAdapt(3, 1));
        channelConfig.setAckNoDelay(true);
        channelConfig.setTimeoutMillis(10000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setCrc32Check(true);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(kcpRttExampleServer, channelConfig, 20003);
    }

    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }

    @Override
    protected void handleReceive0(ByteBuf cast, Ukcp ukcp) throws Exception {
        short curCount = cast.getShort(cast.readerIndex());
        System.out.println(Thread.currentThread().getName() + "  收到消息 " + curCount);

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(cast);
        ukcp.write(byteBuf);

        if (curCount == -1) {
            ukcp.close();
        }
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
    }
}
