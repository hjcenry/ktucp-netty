package test;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import com.hjcenry.net.server.KtucpServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2018/11/2.
 */
public class KtucpRttExampleServer extends SimpleKtucpListener<ByteBuf> {

    public static void main(String[] args) {

        KtucpRttExampleServer kcpRttExampleServer = new KtucpRttExampleServer();

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
        KtucpServer ktucpServer = new KtucpServer();
        ktucpServer.init(kcpRttExampleServer, channelConfig, 20003);
    }

    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + uktucp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }

    @Override
    protected void handleReceive0(ByteBuf cast, Uktucp uktucp) throws Exception {
        short curCount = cast.getShort(cast.readerIndex());
        System.out.println(Thread.currentThread().getName() + "  收到消息 " + curCount);

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(cast);
        uktucp.write(byteBuf);

        if (curCount == -1) {
            uktucp.close();
        }
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
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
    }
}
