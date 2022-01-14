package test;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.SimpleKcpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 重连测试客户端
 * Created by JinMiao
 * 2019-06-27.
 */
public class KcpReconnectExampleClient extends SimpleKcpListener<ByteBuf> {

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        //channelConfig.setAckNoDelay(true);
        //channelConfig.setCrc32Check(true);
        //channelConfig.setTimeoutMillis(10000);
        channelConfig.setConv(55);
        channelConfig.setUseConvChannel(true);

        KtucpClient ktucpClient = new KtucpClient();

        KcpReconnectExampleClient kcpClientRttExample = new KcpReconnectExampleClient();
        ktucpClient.init(kcpClientRttExample, channelConfig, new InetSocketAddress("127.0.0.1", 20004));

        ktucpClient.connect();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ktucpClient.reconnect();
            }
        }, 1000, 1000);
    }

    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        for (int i = 0; i < 100; i++) {
            ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(1024);
            byteBuf.writeInt(i++);
            byte[] bytes = new byte[1020];
            byteBuf.writeBytes(bytes);
            ukcp.write(byteBuf);
            byteBuf.release();
        }
    }

    int j = 0;

    @Override
    protected void handleReceive0(ByteBuf cast, Ukcp ukcp) throws Exception {
        ukcp.write(cast);
        int id = cast.getInt(0);
        //if(j-id%10!=0){
        //    System.out.println("id"+id +"  j" +j);
        //}

        j++;
        if (j % 100000 == 0) {
            System.out.println(Snmp.snmp.toString());
            System.out.println("收到了 返回回去" + j);
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
        System.out.println("连接断开了");
    }


}
