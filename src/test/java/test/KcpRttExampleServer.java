package test;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.KcpListener;
import com.hjcenry.kcp.KcpServer;
import com.hjcenry.kcp.Ukcp;
import io.netty.buffer.ByteBuf;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2018/11/2.
 */
public class KcpRttExampleServer implements KcpListener {

    public static void main(String[] args) {

        KcpRttExampleServer kcpRttExampleServer = new KcpRttExampleServer();

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndwnd(512);
        channelConfig.setRcvwnd(512);
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
    public void onConnected(Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getRemoteAddress());
    }

    @Override
    public void handleReceive(ByteBuf buf, Ukcp kcp) {
        short curCount = buf.getShort(buf.readerIndex());
        System.out.println(Thread.currentThread().getName() + "  收到消息 " + curCount);
        kcp.write(buf);
        if (curCount == -1) {
            kcp.close();
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
