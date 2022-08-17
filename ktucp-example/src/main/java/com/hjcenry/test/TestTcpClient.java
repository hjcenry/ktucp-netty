package com.hjcenry.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName TestTcpClient
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 16:59
 * @Version 1.0
 **/
public class TestTcpClient extends SimpleChannelInboundHandler<ByteBuf> {

    private static Logger logger = LoggerFactory.getLogger(TestKcpClient.class);

    static {
        logger.info("tcp start!!!!");
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        CountDownLatch waiter = new CountDownLatch(0);
        for (int packageCount = 500; packageCount <= 500; packageCount = packageCount + 300) {
//            // 从300个包，测到9000个包
            for (int packageContentSize = 0; packageContentSize <= 10000; packageContentSize = packageContentSize + 200) {
                // 发送字符串长度从200，测到10000

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                startClient(TestServerEnum.LOCAL_TCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                startClient(TestServerEnum.INNER_TCP, packageCount, packageContentSize, waiter);

                //等上一个执行完了，再执行下一个
                waiter = wait(waiter);
                startClient(TestServerEnum.OUTER_TCP, packageCount, packageContentSize, waiter);
            }
        }

        // 固定值测下延迟
        //等上一个执行完了，再执行下一个
        // waiter = wait(waiter);
//        startClient(TestServerEnum.LOCAL_TCP, NetTest.testPackageCount, NetTest.testPackageContentSize, waiter);
        //等上一个执行完了，再执行下一个
        // waiter = wait(waiter);
//        startClient(TestServerEnum.INNER_TCP, NetTest.testPackageCount, NetTest.testPackageContentSize, waiter);
        //等上一个执行完了，再执行下一个
//        waiter = wait(waiter);
//        startClient(TestServerEnum.OUTER_TCP, 100, 10000, waiter);

        long end = System.currentTimeMillis();

        try {
            // 等待服务成功启动。再继续后面的逻辑
            waiter.await(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NetTest.printPlots(NetTest.netStatisticMap);
        System.out.println("执行结束，总耗时:" + (end - start));
        System.exit(0);
    }

    private static CountDownLatch wait(CountDownLatch waiter) {
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

    public static void startClient(TestServerEnum serverEnum, int testPackageCount, int testPackageContentSize, CountDownLatch waiter) throws InterruptedException {
        new TestTcpClient(serverEnum, testPackageCount, testPackageContentSize, waiter).connect(new InetSocketAddress(serverEnum.getHost(), serverEnum.getPort()));
    }

    TestServerEnum serverEnum;
    int testPackageCount;
    int testPackageContentSize;
    CountDownLatch countDownLatch;

    private AtomicInteger counter = new AtomicInteger(1);

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

    private EventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel channel;

    private Map<Integer, NetPacket> packets = new ConcurrentHashMap<>(this.testPackageCount);

    public TestTcpClient(TestServerEnum serverEnum, int testPackageCount, int testPackageContentSize, CountDownLatch countDownLatch) {
        this.serverEnum = serverEnum;
        this.testPackageCount = testPackageCount;
        this.testPackageContentSize = testPackageContentSize;
        this.countDownLatch = countDownLatch;
        // netty connector初始化
        this.group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(group).channel(NioSocketChannel.class);
        this.bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        this.bootstrap.option(ChannelOption.SO_SNDBUF, 2048);
        this.bootstrap.option(ChannelOption.SO_RCVBUF, 8096);
        this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        SimpleChannelInboundHandler<ByteBuf> handler = this;
        this.bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                pipeline.addLast(handler);
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (NetTest.printStatistic) {
            System.out.println("tcp client connect:" + ctx.channel());
        }

        scheduler.scheduleWithFixedDelay(() -> {
            int count = counter.getAndIncrement();
            if (count > this.testPackageCount) {
                return;
            }
            long now = System.currentTimeMillis();
            NetPacket netPacket = new NetPacket(count, now);
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(20);

            NetTest.writeMessageInClient("tcp [" + ctx.channel() + "] ", count, byteBuf, netPacket, now);

            // 写数据
            ctx.channel().writeAndFlush(byteBuf);

            // 记录包数据
            packets.put(netPacket.getId(), netPacket);

        }, NetTest.clientSendMsgInitialDelay, NetTest.clientSendMsgInterval, TimeUnit.MILLISECONDS);// 每10ms发一个包
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        int packetId = NetTest.readMessageAndParsePacketIdInClient("tcp [" + ctx.channel() + "] ", byteBuf, packets);
        if (packetId == -1) {
            return;
        }

        if (packetId >= this.testPackageCount) {
            if (NetTest.printStatistic) {
                System.out.println("tcp [" + ctx.channel() + "] client receive end packet[" + packetId + "]");
            }
            ctx.channel().close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("inactive!!!");
        super.channelInactive(ctx);
        if (NetTest.printStatistic) {
            System.out.println("tcp [" + ctx.channel() + "] close");
        }
        NetStatistic netStatistic = NetTest.logNetStatics(packets, this.serverEnum, this.testPackageContentSize, null);
        NetTest.addNetStatistic(netStatistic);
        this.countDown();

        this.close();
    }

    private void close() {
        this.group.shutdownGracefully();
    }

    public Channel connect(InetSocketAddress... address) throws InterruptedException {
        for (InetSocketAddress addr : address) {
            ChannelFuture future = bootstrap.connect(addr);
            future.sync();
            return future.channel();
        }
        return null;
    }

    private void countDown() {
        this.countDownLatch.countDown();
    }
}
