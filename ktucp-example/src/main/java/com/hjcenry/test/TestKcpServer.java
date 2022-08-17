package com.hjcenry.test;

import com.alibaba.fastjson.JSON;
import com.hjcenry.util.StringUtil;
import io.netty.buffer.ByteBuf;
import kcp.ChannelConfig;
import kcp.KcpListener;
import kcp.KcpServer;
import kcp.Ukcp;

/**
 * @ClassName TestKcp
 * @Description
 * @Author hejincheng
 * @Date 2021/12/20 12:20
 * @Version 1.0
 **/
public class TestKcpServer implements KcpListener {

    public static void main(String[] args) {
        int port = 6666;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }
        startServer(port);
    }

    /**
     * KCP常用参数设置
     *
     * @return
     */
    private static ChannelConfig getChannelConfig() {
        // 发送窗口，接收窗口，mtu的值通过启动参数传进来，可以动态调整测试速度
        int sndWd = 300;
        String sndWdString = System.getProperty("sndWd");
        if (!StringUtil.isEmpty(sndWdString)) {
            sndWd = Integer.parseInt(sndWdString);
        }
        int rcvWd = 300;
        String rcvWdString = System.getProperty("rcvWd");
        if (!StringUtil.isEmpty(rcvWdString)) {
            rcvWd = Integer.parseInt(rcvWdString);
        }
        int mtu = 500;
        String mtuString = System.getProperty("mtu");
        if (!StringUtil.isEmpty(mtuString)) {
            mtu = Integer.parseInt(mtuString);
        }

        //大量客户端测试时服务器或者客户端收不到对方信息，但发送成功，因为bind时候bind到了Ipv6,相同的的ipv4端口被其他进程占用导致， 在启动参数加上 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false 禁用ipv6
        System.setProperty("java.net.preferIPv4Stack", "true");

        ChannelConfig channelConfig = new ChannelConfig();
        //flush策略，这里对C源码做了优化，不单单靠update调度来flush
        //1：在send调用后检查缓冲区如果可以发送直接调用update得到时间并存在ukcp内
        //2：定时任务到了检查ukcp的时间和自己的定时 如果可以发送则直接发送  时间延后则重新定时
        //定时任务发送成功后检测缓冲区  是否触发发送时间
        //3：读事件触发后检测检测缓冲区触发写事件

        // nodelay：是否立即发送，影响rto计算，建议开启
        // interval（10ms-5000ms）：kcp更新间隔，影响rto计算，定时任务（ScheduleTask）驱动间隔，
        // 定时任务的驱动间隔又会影响超时断开，超时重传，快速重传的检测，建议最小值10ms（在机器性能抗的住的前提越小越好）
        // 重传触发次数：2次跳过立即重传
        // 是否关闭拥塞控制：true关闭，false打开，数据量不是特别大的话，建议关闭，打开时会根据mss和对端接收窗口控制自身发送窗口大小
        channelConfig.nodelay(true, 10, 2, true);
        // 发送窗口大小，根据数据量和包大小调试，这里用作者示例中的建议值
        channelConfig.setSndwnd(sndWd);
        // 接收窗口大小，根据数据量和包大小调试，这里用作者示例中的建议值
        channelConfig.setRcvwnd(rcvWd);
        // 传输单元最大值，根据数据量和包大小调试，这里用作者示例中的建议值
        channelConfig.setMtu(mtu);
        // 立即回复ack
        channelConfig.setAckNoDelay(true);
        // 超时断开：客户端多久没有消息，就直接断开
        channelConfig.setTimeoutMillis(5000);
        // 用conv做连接唯一标识（不用这个，用地址做标识）
        channelConfig.setUseConvChannel(false);

        // 还提供crc和fec功能可用
//        channelConfig.setCrc32Check(false);
//        channelConfig.setFecAdapt(new FecAdapt(3,1));
        return channelConfig;
    }

    @Override
    public void onConnected(Ukcp ukcp) {
        System.out.println("kcp connect:" + ukcp);
    }

    @Override
    public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
        ByteBuf scByteBuf = NetTest.readMessageAndBuildReplyInServer(byteBuf);
        if (scByteBuf == null) {
            return;
        }
        // 写数据
        ukcp.write(scByteBuf);
        scByteBuf.release();
    }

    @Override
    public void handleException(Throwable throwable, Ukcp ukcp) {
        System.out.println("kcp [" + ukcp + "] server exception:" + throwable);
    }

    @Override
    public void handleClose(Ukcp ukcp) {
        System.out.println("kcp [" + ukcp + "] close");
    }

    public static void startServer(int port) {
        KcpServer kcpServer = new KcpServer();

        ChannelConfig channelConfig = getChannelConfig();

        kcpServer.init(new TestKcpServer(), channelConfig, port);
        System.out.println("kcp server start bind:" + port + "...");
        System.out.println("====================================");
        System.out.println(JSON.toJSONString(channelConfig));
    }

}
