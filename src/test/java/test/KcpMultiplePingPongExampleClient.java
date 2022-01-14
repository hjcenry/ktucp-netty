package test;

import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 测试多连接吞吐量
 * Created by JinMiao
 * 2019-06-27.
 */
public class KcpMultiplePingPongExampleClient implements KcpListener {

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 0, true);
        channelConfig.setSndWnd(256);
        channelConfig.setRcvWnd(256);
        channelConfig.setMtu(400);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        //channelConfig.setAckNoDelay(true);

        //channelConfig.setCrc32Check(true);
        //channelConfig.setTimeoutMillis(10000);

        KtucpClient ktucpClient = new KtucpClient();
        KcpMultiplePingPongExampleClient kcpMultiplePingPongExampleClient = new KcpMultiplePingPongExampleClient();
        ktucpClient.init(kcpMultiplePingPongExampleClient, channelConfig, new InetSocketAddress("127.0.0.1", 10011));

        int clientNumber = 1000;
        for (int i = 0; i < clientNumber; i++) {
            channelConfig.setConv(i);
            ktucpClient.connect();
        }
    }

    Timer timer = new Timer();

    @Override
    public void onConnected(int netId, Ukcp ukcp) {
        System.out.println(ukcp.getConv());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(1004);
                byteBuf.writeInt(1);
                byte[] bytes = new byte[1000];
                byteBuf.writeBytes(bytes);
                ukcp.write(byteBuf);
                byteBuf.release();
            }
        }, 100, 100);
    }

    @Override
    public void handleReceive(Object object, Ukcp ukcp) {
        //System.out.println("收到消息");
        //ukcp.writeMessage(byteBuf);
        //int id = byteBuf.getInt(0);
        //if(j-id%10!=0){
        //    System.out.println("id"+id +"  j" +j);
        //}
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
        System.out.println("连接断开了" + kcp.getConv());
    }


}
