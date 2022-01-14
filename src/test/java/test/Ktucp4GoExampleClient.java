package test;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.net.client.KtucpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.net.InetSocketAddress;

/**
 * 与go版本兼容的客户端
 * Created by JinMiao
 * 2019/11/29.
 */
public class Ktucp4GoExampleClient implements KtucpListener {

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


        KtucpClient ktucpClient = new KtucpClient();
        Ktucp4GoExampleClient kcpGoExampleClient = new Ktucp4GoExampleClient();
        ktucpClient.init(kcpGoExampleClient, channelConfig, new InetSocketAddress("127.0.0.1", 10000));
        ktucpClient.connect();

        Uktucp uktucp = ktucpClient.getUkcp();
        String msg = "hello!!!!!11111111111111111111111111";
        byte[] bytes = msg.getBytes();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer(bytes.length);
        byteBuf.writeBytes(bytes);
        uktucp.write(byteBuf);

    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleTimeout!!!:" + uktucp);
    }

    @Override
    public void onConnected(int netId, Uktucp uktucp) {

    }

    @Override
    public void handleReceive(Object object, Uktucp uktucp) {

    }

    @Override
    public void handleException(Throwable ex, Uktucp uktucp) {

    }

    @Override
    public void handleClose(Uktucp uktucp) {

    }
}
