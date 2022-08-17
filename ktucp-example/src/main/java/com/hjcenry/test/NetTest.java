package com.hjcenry.test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hjcenry.test.proto.Battle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName NetTest
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 18:02
 * @Version 1.0
 **/
public class NetTest {

    private static Logger logger = LoggerFactory.getLogger(NetTest.class);

    public static final boolean printStatistic = true;

    // 测试包数量
    public static final int testPackageCount = 1000;
    // 客户端发包间隔
    public static final int clientSendMsgInitialDelay = 1000;
    public static final int clientSendMsgInterval = 100;
    // 客户端包大小
    public static final int testPackageContentSize = 10000;
    // 客户端包中字符串啊内容
    private static final String content;

    static {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < testPackageContentSize; i++) {
            builder.append("a");
        }
        content = builder.toString();
    }

    /**
     * 读消息并构建回复消息
     *
     * @param byteBuf
     * @return
     */
    public static ByteBuf readMessageAndBuildReplyInServer(ByteBuf byteBuf) {
        byte[] bytes = TestNetCoder.decode(byteBuf);
        Battle.TestKcp msg = null;
        try {
            if (bytes != null) {
                msg = Battle.TestKcp.parseFrom(bytes);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        if (msg == null) {
            return null;
        }
        ByteBuf scByteBuf = Unpooled.buffer(0);
        // 回心跳消息
        Battle.TestKcp_S2C.Builder builder = Battle.TestKcp_S2C.newBuilder();
        builder.setClientTime(msg.getClientTime());
        long now = System.currentTimeMillis();
        builder.setServerTime(now);
        // 消息包编号
        builder.setMsgIndex(msg.getMsgIndex());
        // 文本内容
        builder.setContent(msg.getContent());

        // 编码
        Battle.TestKcp_S2C s2c = builder.build();
        TestNetCoder.encode(msg.getMsgIndex(), s2c, scByteBuf);
        return scByteBuf;
    }

    public static NetStatistic logNetStatics(Map<Integer, NetPacket> packets, TestServerEnum serverEnum, int testPackageContentSize, String custom) {
        // 统计网络数据
        long totalReceiveBytes = 0;
        long totalSendBytes = 0;
        long totalDelay = 0;

        int statisticCount = 0;
        // 统计
        int allCount = packets.values().size();

        // 最高延迟
        float highestDelay = -1f;
        // 最低延迟
        float lowestDelay = -1f;

        for (NetPacket netPacket : packets.values()) {
            if (!netPacket.isValid()) {
                if (printStatistic) {
                    System.out.println(String.format("Packet[%d] %s %s", netPacket.getId(), "NOT VALID", netPacket));
                }
                continue;
            }
            statisticCount++;

            if (allCount == 1 || netPacket.getId() > 1) {
                // 如果包大于1，不统计第一个包，因为第一个包可能包含建立连接的消耗，排除这部分影响
                totalReceiveBytes += netPacket.getReceivePacketBytes();
                totalSendBytes += netPacket.getSendPacketBytes();
                float netDelay = netPacket.totalDelay();
                totalDelay += netDelay;

                if (highestDelay == -1 || netDelay >= highestDelay) {
                    highestDelay = netDelay;
                }
                if (lowestDelay == -1 || netDelay <= lowestDelay) {
                    lowestDelay = netDelay;
                }
            }
        }

        // 打印
        NetStatistic netStatistic = new NetStatistic(serverEnum, statisticCount, totalReceiveBytes, totalSendBytes, totalDelay, testPackageContentSize, custom, highestDelay, lowestDelay);
        String log = netStatistic.getStatisticLog();
        if (printStatistic) {
            System.out.println(log);
            logger.info(log);
        }
        return netStatistic;
    }

    public static int readMessageAndParsePacketIdInClient(String logHead, ByteBuf byteBuf, Map<Integer, NetPacket> packets) {
        byte[] bytes = TestNetCoder.decode(byteBuf);
        if (bytes == null) {
            return -1;
        }
        Battle.TestKcp_S2C msg = null;
        try {
            msg = Battle.TestKcp_S2C.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return -1;
        }

        int packetId = msg.getMsgIndex();
        // 记录包数据
        NetPacket packet = packets.get(packetId);
        if (packet == null) {
            if (printStatistic) {
                System.err.println(logHead + "client receive packet[" + packetId + "] null:");
            }
            return -1;
        }
        packet.setServerReceiveTime(msg.getServerTime());
        packet.setReturnTime(System.currentTimeMillis());
        packet.addReceivePackets(bytes.length + 8);
//        System.out.println(logHead + "client receive:" + msg);
        return packetId;
    }

    public static void writeMessageInClient(String logHead, int code, ByteBuf byteBuf, NetPacket packet, long now) {
        // 发送心跳消息
        Battle.TestKcp.Builder builder = Battle.TestKcp.newBuilder();
        builder.setClientTime(now);
        builder.setMsgIndex(code);
        builder.setContent(content);

        // 编码
        Battle.TestKcp msg = builder.build();
        int len = TestNetCoder.encode(code, msg, byteBuf);

        packet.addSendPacketBytes(len + 8);

//        System.out.println(logHead + "client send:" + msg);
    }

    public static void printPlots(Map<TestServerEnum, Map<Integer, Map<Integer, NetStatistic>>> netStatisticMap) {
        for (Map.Entry<TestServerEnum, Map<Integer, Map<Integer, NetStatistic>>> testServerEnumMapEntry : netStatisticMap.entrySet()) {
            TestServerEnum testServerEnum = testServerEnumMapEntry.getKey();
            Map<Integer, Map<Integer, NetStatistic>> testPackageCountMap = testServerEnumMapEntry.getValue();

            StringBuilder print = new StringBuilder("=================================\n");
            for (Map.Entry<Integer, Map<Integer, NetStatistic>> testPackageMapEntry : testPackageCountMap.entrySet()) {
                int testPackageCount = testPackageMapEntry.getKey();
                Map<Integer, NetStatistic> statisticMap = testPackageMapEntry.getValue();
                print.append("测试服务器:\t").append(testServerEnum).append("\n");
                print.append("发送包数量:\t").append(testPackageCount).append("\n");

                StringBuilder packageSizeBuilder = new StringBuilder("(");
                StringBuilder delayBuilder = new StringBuilder("(");
                StringBuilder highestDelayBuilder = new StringBuilder("(");
                StringBuilder lowestDelayBuilder = new StringBuilder("(");

                for (Map.Entry<Integer, NetStatistic> entry : statisticMap.entrySet()) {
                    int packageContentSize = entry.getKey();
                    NetStatistic value = entry.getValue();
                    packageSizeBuilder.append(packageContentSize).append(",");
                    delayBuilder.append(value.getAvgDelay()).append(",");
                    highestDelayBuilder.append(value.getHighestDelay()).append(",");
                    lowestDelayBuilder.append(value.getLowestDelay()).append(",");
                }
                print.append("发送包长度:\t").append(packageSizeBuilder.substring(0, packageSizeBuilder.length() - 1)).append(")").append("\n");
                print.append("平均延迟(ms):\t").append(delayBuilder.substring(0, delayBuilder.length() - 1)).append(")").append("\n");
                print.append("最高延迟(ms):\t").append(highestDelayBuilder.substring(0, highestDelayBuilder.length() - 1)).append(")").append("\n");
                print.append("最低延迟(ms):\t").append(lowestDelayBuilder.substring(0, lowestDelayBuilder.length() - 1)).append(")").append("\n");
                print.append("---------------------------------------------------\n");
                print.append("python code:\n");
                print.append(String.format("" +
                                "%s_%d = {\n" +
                                "        'plotsX': [%s],\n" +
                                "        'plotsY': [%s],\n" +
                                "        'label': \"%s环境(%d条消息)\",\n" +
                                "    }\n" +
                                "%s_%d_high_delay = {\n" +
                                "        'plotsX': [%s],\n" +
                                "        'plotsY': [%s],\n" +
                                "        'label': \"%s环境(%d条消息)\",\n" +
                                "    }\n" +
                                "%s_%d_low_delay = {\n" +
                                "        'plotsX': [%s],\n" +
                                "        'plotsY': [%s],\n" +
                                "        'label': \"%s环境(%d条消息)\",\n" +
                                "    }" +
                                "",
                        testServerEnum.name().toLowerCase(), testPackageCount,
                        packageSizeBuilder.substring(1, packageSizeBuilder.length() - 1),
                        delayBuilder.substring(1, delayBuilder.length() - 1),
                        testServerEnum.name().toLowerCase(), testPackageCount,

                        testServerEnum.name().toLowerCase(), testPackageCount,
                        packageSizeBuilder.substring(1, packageSizeBuilder.length() - 1),
                        highestDelayBuilder.substring(1, highestDelayBuilder.length() - 1),
                        testServerEnum.name().toLowerCase(), testPackageCount,

                        testServerEnum.name().toLowerCase(), testPackageCount,
                        packageSizeBuilder.substring(1, packageSizeBuilder.length() - 1),
                        lowestDelayBuilder.substring(1, lowestDelayBuilder.length() - 1),
                        testServerEnum.name().toLowerCase(), testPackageCount
                ));
                print.append("---------------------------------------------------\n");
            }

            logger.info("\n" + print);
        }
    }

    /**
     * 系统参数为空就设置默认值
     */
    public static void setDefaultProp(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    /**
     * <服务器, 发送包数量, 发送字符串大小, 统计数据>
     */
    static Map<TestServerEnum, Map<Integer, Map<Integer, NetStatistic>>> netStatisticMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        CountDownLatch waiter = new CountDownLatch(0);
        for (int packageCount = 500; packageCount <= 500; packageCount = packageCount + 300) {
            // 发送包数量越多，最终平均值越精确
            for (int packageContentSize = 0; packageContentSize <= 10000; packageContentSize = packageContentSize + 200) {
                // 发送字符串长度从0，测到10000

                waiter = wait(waiter);
                //等上一个执行完了，再执行下一个
                TestKcpClient.startClient(TestServerEnum.LOCAL_KCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                TestKcpClient.startClient(TestServerEnum.INNER_KCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                TestKcpClient.startClient(TestServerEnum.OUTER_KCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                TestTcpClient.startClient(TestServerEnum.LOCAL_TCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                TestTcpClient.startClient(TestServerEnum.INNER_TCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                TestTcpClient.startClient(TestServerEnum.OUTER_TCP, packageCount, packageContentSize, waiter);
            }
        }

        long end = System.currentTimeMillis();

        try {
            // 等待服务成功启动。再继续后面的逻辑
//            waiter.await(10, TimeUnit.MINUTES);
            waiter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NetTest.printPlots(netStatisticMap);
        System.out.println("执行结束，总耗时:" + (end - start));
        System.exit(0);
    }

    public static CountDownLatch wait(CountDownLatch waiter) {
        //等上一个执行完了，再执行下一个
        try {
            waiter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 重置计数器
        waiter = new CountDownLatch(1);
        return waiter;
    }

    public static synchronized void addNetStatistic(NetStatistic statistic) {
        TestServerEnum testServerEnum = statistic.getServerEnum();
        int testPacketCount = statistic.getTestPacketCount();
        int testPackageContentSize = statistic.getTestPackageContentSize();

        Map<Integer, Map<Integer, NetStatistic>> testServerMap = netStatisticMap.get(testServerEnum);
        if (testServerMap == null) {
            testServerMap = new ConcurrentHashMap<>();
            netStatisticMap.put(testServerEnum, testServerMap);
        }

        Map<Integer, NetStatistic> testPackageCountMap = testServerMap.computeIfAbsent(testPacketCount, k -> new TreeMap<>());
        testPackageCountMap.put(testPackageContentSize, statistic);
    }
}
