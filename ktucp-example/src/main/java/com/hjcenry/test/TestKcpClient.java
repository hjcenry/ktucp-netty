package com.hjcenry.test;

import com.backblaze.erasure.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import kcp.ChannelConfig;
import kcp.KcpClient;
import kcp.KcpListener;
import kcp.Ukcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName TestKcpClient
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 11:43
 * @Version 1.0
 **/
public class TestKcpClient implements KcpListener {

    private static Logger logger = LoggerFactory.getLogger(TestKcpClient.class);

    static {
        logger.info("kcp start!!!!");
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        CountDownLatch waiter = new CountDownLatch(0);
        for (int packageCount = 500; packageCount <= 500; packageCount = packageCount + 300) {
            // 从300个包，测到9000个包
            for (int packageContentSize = 200; packageContentSize <= 10000; packageContentSize = packageContentSize + 200) {
                // 发送字符串长度从200，测到10000

                waiter = NetTest.wait(waiter);
                //等上一个执行完了，再执行下一个
                startClient(TestServerEnum.LOCAL_KCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = NetTest.wait(waiter);
                startClient(TestServerEnum.INNER_KCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = NetTest.wait(waiter);
                startClient(TestServerEnum.OUTER_KCP, packageCount, packageContentSize, waiter);
            }
        }

        // 固定值测下延迟
//        startClient(TestServerEnum.LOCAL_KCP, NetTest.testPackageCount, NetTest.testPackageContentSize, waiter);
//        startClient(TestServerEnum.INNER_KCP, NetTest.testPackageCount, NetTest.testPackageContentSize, waiter);
//        startClient(TestServerEnum.OUTER_KCP, NetTest.testPackageCount, NetTest.testPackageContentSize, waiter);

        long end = System.currentTimeMillis();

        try {
            // 等待服务成功启动。再继续后面的逻辑
            waiter.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NetTest.printPlots(NetTest.netStatisticMap);
        System.out.println("执行结束，总耗时:" + (end - start));
        System.exit(0);
    }

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

    KcpClient kcpClient;

    TestServerEnum serverEnum;

    int testPackageCount;

    int testPackageContentSize;

    CountDownLatch countDownLatch;

    public TestKcpClient(KcpClient kcpClient, TestServerEnum serverEnum, int testPackageCount, int testPackageContentSize, CountDownLatch countDownLatch) {
        this.kcpClient = kcpClient;
        this.serverEnum = serverEnum;
        this.testPackageCount = testPackageCount;
        this.testPackageContentSize = testPackageContentSize;
        this.countDownLatch = countDownLatch;
    }

    private AtomicInteger counter = new AtomicInteger(1);

    private Map<Integer, NetPacket> packets = new ConcurrentHashMap<>(this.testPackageCount);

    @Override
    public void onConnected(Ukcp ukcp) {
        if (NetTest.printStatistic) {
            System.out.println("kcp client connect " + this.serverEnum + ":" + ukcp);
        }

        scheduler.scheduleWithFixedDelay(() -> {
            int count = counter.getAndIncrement();
            if (count > this.testPackageCount) {
                return;
            }
            long now = System.currentTimeMillis();
            NetPacket netPacket = new NetPacket(count, now);
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.heapBuffer();

            NetTest.writeMessageInClient("kcp [" + ukcp + "] ", count, byteBuf, netPacket, now);

            // 写数据
            ukcp.write(byteBuf);
            byteBuf.release();

            // 记录包数据
            packets.put(netPacket.getId(), netPacket);

        }, NetTest.clientSendMsgInterval, NetTest.clientSendMsgInterval, TimeUnit.MILLISECONDS);// 每10ms发一个包
    }

    @Override
    public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
        int packetId = NetTest.readMessageAndParsePacketIdInClient("kcp [" + ukcp + "] ", byteBuf, packets);
        if (packetId == -1) {
            return;
        }

        if (packetId >= this.testPackageCount) {
//            System.out.println("kcp [" + ukcp + "] client receive end packet");
            kcpClient.stop();
        }
    }

    @Override
    public void handleException(Throwable throwable, Ukcp ukcp) {
        if (NetTest.printStatistic) {
            System.out.println("kcp [" + ukcp + "] client exception:" + throwable);
        }
//        this.countDown();
//        System.exit(0);
    }

    @Override
    public void handleClose(Ukcp ukcp) {
        if (NetTest.printStatistic) {
            System.out.println("kcp [" + ukcp + "] " + this.serverEnum + " close");
        }
        String snmp = String.format("KCP底层统计: \n" +
                        "发送包:%d \t发送数据（有效发送数据/总发送数据）:%fMB/%fMB\n" +
                        "接收包:%d \t接收数据（有效接收数据/总接收数据）:%fMB/%fMB \n" +
                        "总共重发数:%d\n" +
                        "快速重发数:%d\n" +
                        "空闲快速重发数:%d\n" +
                        "超时重发数:%d\n" +
                        "收到重复包数量:%d\n",
                Snmp.snmp.OutPkts.intValue(),
                Snmp.snmp.BytesSent.doubleValue() / 1024d / 1024d,
                Snmp.snmp.OutBytes.doubleValue() / 1024d / 1024d,
                Snmp.snmp.InPkts.intValue(),
                Snmp.snmp.BytesReceived.doubleValue() / 1024d / 1024d,
                Snmp.snmp.InBytes.doubleValue() / 1024d / 1024d,
                Snmp.snmp.RetransSegs.intValue(),
                Snmp.snmp.FastRetransSegs.intValue(),
                Snmp.snmp.EarlyRetransSegs.intValue(),
                Snmp.snmp.LostSegs.intValue(),
                Snmp.snmp.RepeatSegs.intValue()
        );
        NetStatistic netStatistic = NetTest.logNetStatics(packets, this.serverEnum, this.testPackageContentSize, snmp);
        NetTest.addNetStatistic(netStatistic);
        this.countDown();

        this.close();
    }

    private void close() {
        this.kcpClient.stop();
    }

    public static void startClient(TestServerEnum serverEnum, int testPackageCount, int testPackageContentSize, CountDownLatch waiter) {
        ChannelConfig channelConfig = new ChannelConfig();
        // nodelay，40ms间隔，2次跳过立即重传，使用拥塞控制
        channelConfig.nodelay(true, 40, 2, true);
        // 发送窗口大小
        channelConfig.setSndwnd(300);
        // 接收窗口大小
        channelConfig.setRcvwnd(300);
        // 传输单元最大值
        channelConfig.setMtu(500);
        // 立即回复ack
        channelConfig.setAckNoDelay(true);
        // 3s超時斷開
        channelConfig.setTimeoutMillis(3000);
        KcpClient kcpClient = new KcpClient();
        kcpClient.init(channelConfig);

        kcpClient.connect(new InetSocketAddress(serverEnum.getHost(), serverEnum.getPort()), channelConfig, new TestKcpClient(kcpClient, serverEnum, testPackageCount, testPackageContentSize, waiter));
    }

    private void countDown() {
        this.countDownLatch.countDown();
    }

}
