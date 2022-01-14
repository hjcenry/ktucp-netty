package test;

import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.net.InetSocketAddress;

/**
 * 测试大量连接不通讯的例子
 * Created by JinMiao
 * 2019-07-10.
 */
public class KtucpIdleExampleClient implements KtucpListener {

    public static void main(String[] args) {

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

        KtucpClient ktucpClient = new KtucpClient();
        KtucpIdleExampleClient kcpIdleExampleClient = new KtucpIdleExampleClient();
        ktucpClient.init(kcpIdleExampleClient, channelConfig, new InetSocketAddress("127.0.0.1", 10020));

        for (int i = 0; i < 3; i++) {
            if (i % 1000 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ;
            //kcpClient.connect(new InetSocketAddress("10.60.100.191", 10020), channelConfig, kcpIdleExampleClient);
            ktucpClient.connect();
        }

    }

    int i = 0;

    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(124);
        byteBuf.writeInt(i++);
        byte[] bytes = new byte[120];
        byteBuf.writeBytes(bytes);
        uktucp.write(byteBuf);
        byteBuf.release();
    }
    //int j =0;

    @Override
    public void handleReceive(Object object, Uktucp uktucp) {
        //ukcp.write(byteBuf);
        //int id = byteBuf.getInt(0);
        ////if(j-id%10!=0){
        ////    System.out.println("id"+id +"  j" +j);
        ////}
        //
        //j++;
        //if(j%100000==0){
        //    System.out.println(Snmp.snmp.toString());
        //    System.out.println("收到了 返回回去"+j);
        //}
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
        System.out.println("连接断开了");
    }


}