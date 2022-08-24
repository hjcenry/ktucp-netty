package com.hjcenry.multinet;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.fec.FecAdapt;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.IKtucpServerStartUpCallback;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import com.hjcenry.net.callback.StartUpNettyServerCallBack;
import com.hjcenry.net.client.KtucpClient;
import com.hjcenry.net.server.KtucpServer;
import com.hjcenry.net.tcp.INettyChannelEvent;
import com.hjcenry.net.tcp.TcpChannelConfig;
import com.hjcenry.net.udp.UdpChannelConfig;
import com.hjcenry.system.SystemOS;
import com.hjcenry.time.SystemTimeServiceImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2019-06-26.
 */
public class KtucpMultiNetExampleClient extends SimpleKtucpListener<ByteBuf> {
    /**
     * tcp网络id
     */
    private static int tcpNetId;
    /**
     * udp网络id
     */
    private static int udpNetId;

    private final ByteBuf data;

    private int[] rtts;

    private volatile int count;

    private ScheduledExecutorService scheduleSrv;

    private ScheduledFuture<?> future = null;

    private final long startTime;

    public KtucpMultiNetExampleClient() {
        data = Unpooled.buffer(200);
        for (int i = 0; i < data.capacity(); i++) {
            data.writeByte((byte) i);
        }

        rtts = new int[300];
        for (int i = 0; i < rtts.length; i++) {
            rtts[i] = -1;
        }
        startTime = System.currentTimeMillis();
        scheduleSrv = new ScheduledThreadPoolExecutor(1);
    }

    public static void main(String[] args) {
        ChannelConfig channelConfig = getChannelConfig(8888, 8888);
        KtucpMultiNetExampleClient kcpClientRttExample = new KtucpMultiNetExampleClient();
        // 初始客户端
        KtucpClient ktucpClient = new KtucpClient();
        // 指定编解码接口
        // 配置KtucpListener继承SimpleKtucpListener<I>，可以灵活处理项目自定义消息类型，不指定，则按原始ByteBuf处理
        IMessageEncoder encoder = null;
        IMessageDecoder decoder = null;
        ktucpClient.init(kcpClientRttExample, channelConfig, encoder, decoder);
        // 连接
        ktucpClient.connect();
    }

    private static ChannelConfig getChannelConfig(int tcpPort, int udpPort) {
        ChannelConfig channelConfig = new ChannelConfig();
        // KCP基本参数
        // 客户端需要设置convId
        channelConfig.setConv(55);
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(512);
        channelConfig.setRcvWnd(512);
        channelConfig.setMtu(512);
        // KCP超时时间
        channelConfig.setTimeoutMillis(20 * 1000L);
        // KCP超时时是否关闭KCP连接，默认true
        channelConfig.setKcpIdleTimeoutClose(true);
        // 是否使用fec
//        channelConfig.setFecAdapt(new FecAdapt(10, 3));
        // 发送包立即调用flush 延迟低一些  cpu增加  如果interval值很小 建议关闭该参数
        channelConfig.setFastFlush(true);
        // 是否使用crc32校验
        channelConfig.setCrc32Check(false);
        // 是否使用conv(多网络目前仅能使用conv)
        channelConfig.setUseConvChannel(true);
        // 网络切换频繁处理参数，不配则不处理
        channelConfig.setNetChangeMinPeriod(0L);
        channelConfig.setNetChangeMaxCount(0);
        // 可使用自己的缓存时间系统
        channelConfig.setTimeService(new SystemTimeServiceImpl());
        channelConfig.setServerStartUpCallback(new IKtucpServerStartUpCallback() {
            @Override
            public void apply() {
                System.out.println("KCP start success!!!");
            }
        });

        // 添加一个TCP网络配置
        channelConfig.addNetChannelConfig(getTcpChannelConfig(tcpPort));
        // 添加一个UDP网络配置
        channelConfig.addNetChannelConfig(getUdpChannelConfig(udpPort));

        return channelConfig;
    }

    private static TcpChannelConfig getTcpChannelConfig(int port) {
        TcpChannelConfig tcpChannelConfig = TcpChannelConfig.buildClientConfig(new InetSocketAddress("127.0.0.1", port));
        // 配置Channel闲置超时时间
        tcpChannelConfig.setReadIdleTime(20 * 1000L);
        tcpChannelConfig.setWriteIdleTime(20 * 1000L);
        tcpChannelConfig.setAllIdleTime(20 * 1000L);
        // 配置粘包拆包处理参数，不配则不处理粘包拆包
        tcpChannelConfig.setMaxFrameLength(Integer.MAX_VALUE);
        tcpChannelConfig.setLengthFieldOffset(0);
        tcpChannelConfig.setLengthFieldLength(4);
        tcpChannelConfig.setLengthAdjustment(0);
        tcpChannelConfig.setInitialBytesToStrip(4);
        // 设置线程数量
        tcpChannelConfig.setBossThreadNum(1);
        tcpChannelConfig.setIoThreadNum(SystemOS.CPU_NUM);
        // 监听Netty事件
        tcpChannelConfig.setNettyEventTrigger(new INettyChannelEvent() {
            @Override
            public void onChannelActive(Channel channel) {
                System.out.println("[+tcp channel]:" + channel);
            }

            @Override
            public void onChannelInactive(Channel channel, Uktucp uktucp) {
                // uktucp maybe null
                System.out.println("[-tcp channel]:" + channel + " uktucp:" + uktucp);
            }

            @Override
            public void onChannelRead(Channel channel, Uktucp uktucp, ByteBuf byteBuf) {
                System.out.println("[tcp channel]:" + channel + " uktucp:" + uktucp + " channel read");
            }

            @Override
            public void onUserEventTriggered(Channel channel, Uktucp uktucp, Object evt) {
                System.out.println("[tcp channel]:" + channel + " uktucp:" + uktucp + " evt:" + evt);
            }
        });
        // TCP激活成功回调
        tcpChannelConfig.setActiveCallback(new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future, int netId) {
                System.out.println("tcp start success");
                tcpNetId = netId;
            }
        });
        // 可以自定义netId，只要不重复就可以
        // tcpChannelConfig.setNetId(0);
        // 配置TCP参数
        tcpChannelConfig.addTcpChildChannelOption(ChannelOption.SO_KEEPALIVE, true);
        tcpChannelConfig.addTcpServerChannelOption(ChannelOption.SO_KEEPALIVE, true);
        // 可添加自定义ChildHandler
        tcpChannelConfig.addChannelHandler(null);

        return tcpChannelConfig;
    }

    private static UdpChannelConfig getUdpChannelConfig(int port) {
        UdpChannelConfig udpChannelConfig = UdpChannelConfig.buildClientConfig(new InetSocketAddress("127.0.0.1", port));
        // 设置线程数量
        udpChannelConfig.setBossThreadNum(SystemOS.CPU_NUM);
        // 监听Netty事件
        udpChannelConfig.setNettyEventTrigger(new INettyChannelEvent() {
            @Override
            public void onChannelActive(Channel channel) {
                System.out.println("[+udp channel]:" + channel);
            }

            @Override
            public void onChannelInactive(Channel channel, Uktucp uktucp) {
                // uktucp maybe null
                System.out.println("[-udp channel]:" + channel + " uktucp:" + uktucp);
            }

            @Override
            public void onChannelRead(Channel channel, Uktucp uktucp, ByteBuf byteBuf) {
                System.out.println("[udp channel]:" + channel + " uktucp:" + uktucp + " channel read");
            }

            @Override
            public void onUserEventTriggered(Channel channel, Uktucp uktucp, Object evt) {
                System.out.println("[udp channel]:" + channel + " uktucp:" + uktucp + " evt:" + evt);
            }
        });
        // UDP激活成功回调
        udpChannelConfig.setActiveCallback(new StartUpNettyServerCallBack() {
            @Override
            public void apply(Future<Void> future, int netId) {
                System.out.println("udp start success");
                udpNetId = netId;
            }
        });
        // 可以自定义netId，只要不重复就可以
        // udpChannelConfig.setNetId(0);
        // 配置TCP参数
        udpChannelConfig.addUdpChannelOption(ChannelOption.SO_REUSEADDR, true);
        // 可添加自定义ChildHandler
        udpChannelConfig.addChannelHandler(null);
        return udpChannelConfig;
    }


    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        future = scheduleSrv.scheduleWithFixedDelay(() -> {
            ByteBuf byteBuf = rttMsg(++count);
            uktucp.write(byteBuf);
            if (count >= rtts.length) {
                // finish
                future.cancel(true);
                byteBuf = rttMsg(-1);
                uktucp.write(byteBuf);
            }
        }, 20, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void handleReceive0(ByteBuf cast, Uktucp uktucp) throws Exception {
        int curCount = cast.readShort();

        if (curCount == -1) {
            scheduleSrv.schedule(new Runnable() {
                @Override
                public void run() {
                    int sum = 0;
                    for (int rtt : rtts) {
                        sum += rtt;
                    }
                    System.out.println("average: " + (sum / rtts.length));
                    System.out.println(Snmp.snmp.toString());
                    uktucp.close();
                    //ukcp.setTimeoutMillis(System.currentTimeMillis());
                    System.exit(0);
                }
            }, 3, TimeUnit.SECONDS);
        } else {
            int idx = curCount - 1;
            long time = cast.readInt();
            if (rtts[idx] != -1) {
                System.out.println("???");
            }
            //log.info("rcv count {} {}", curCount, System.currentTimeMillis());
            rtts[idx] = (int) (System.currentTimeMillis() - startTime - time);
            System.out.println("rtt : " + curCount + "  " + rtts[idx]);

            // 演示网络切换，可在任意时刻随意切换
            if (uktucp.user().getCurrentNetId() == tcpNetId) {
                uktucp.changeCurrentNetId(udpNetId);
            } else if (uktucp.user().getCurrentNetId() == udpNetId) {
                uktucp.changeCurrentNetId(tcpNetId);
            }

            // 强制使用某一个网络
            // uktucp.changeForceUseNet(udpNetId);
            // uktucp.resetForceUseNet();
        }
    }

    @Override
    public void handleException(Throwable ex, Uktucp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Uktucp kcp) {
        scheduleSrv.shutdown();
        try {
            scheduleSrv.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int sum = 0;
        int max = 0;
        for (int rtt : rtts) {
            if (rtt > max) {
                max = rtt;
            }
            sum += rtt;
        }
        System.out.println("average: " + (sum / rtts.length) + " max:" + max);
        System.out.println(Snmp.snmp.toString());
        System.out.println("lost percent: " + (Snmp.snmp.getRetransmitSegments().doubleValue() / Snmp.snmp.getOutPackets().doubleValue()));


    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleTimeout!!!:" + uktucp);
    }

    /**
     * count+timestamp+dataLen+data
     *
     * @param count
     * @return
     */
    public ByteBuf rttMsg(int count) {
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeShort(count);
        buf.writeInt((int) (System.currentTimeMillis() - startTime));

        //int dataLen = new Random().nextInt(200);
        //buf.writeBytes(new byte[dataLen]);

        int dataLen = data.readableBytes();
        buf.writeShort(dataLen);
        buf.writeBytes(data, data.readerIndex(), dataLen);

        return buf;
    }

}
