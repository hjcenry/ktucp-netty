package test;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.kcp.listener.KcpListener;
import com.hjcenry.net.client.KcpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.net.InetSocketAddress;

/**
 * 与go版本兼容的客户端
 * Created by JinMiao
 * 2019/11/29.
 */
public class Kcp4GoExampleClient implements KcpListener {

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        channelConfig.setFecAdapt(new FecAdapt(10, 3));
        channelConfig.setAckNoDelay(false);
        //channelConfig.setTimeoutMillis(10000);

        //禁用参数
        channelConfig.setCrc32Check(false);
        channelConfig.setAckMaskSize(0);


        KcpClient kcpClient = new KcpClient();
        Kcp4GoExampleClient kcpGoExampleClient = new Kcp4GoExampleClient();
        kcpClient.init(kcpGoExampleClient, channelConfig, new InetSocketAddress("127.0.0.1", 10000));
        kcpClient.connect();

        Ukcp ukcp = kcpClient.getUkcp();
        String msg = "hello!!!!!11111111111111111111111111";
        byte[] bytes = msg.getBytes();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer(bytes.length);
        byteBuf.writeBytes(bytes);
        ukcp.write(byteBuf);

    }

    @Override
    public void handleIdleTimeout(Ukcp ukcp) {
        System.out.println("handleTimeout!!!:" + ukcp);
    }

    @Override
    public void onConnected(int netId, Ukcp ukcp) {

    }

    @Override
    public void handleReceive(Object object, Ukcp ukcp) {

    }

    @Override
    public void handleException(Throwable ex, Ukcp ukcp) {

    }

    @Override
    public void handleClose(Ukcp ukcp) {

    }
}
