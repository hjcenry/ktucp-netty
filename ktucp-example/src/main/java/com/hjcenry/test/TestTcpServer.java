package com.hjcenry.test;

import com.hjcenry.util.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
/**
 * @ClassName TestTcpServer
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 16:59
 * @Version 1.0
 **/
public class TestTcpServer extends SimpleChannelInboundHandler<ByteBuf> {

    public static void main(String[] args) {
        int port = 8888;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }
        new TestTcpServer(port);
    }

    /**
     * TCP参数
     */
    private void init() {
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // 发送窗口，接收窗口的值通过启动参数传进来，可以动态调整测试速度
        int sndWd = 2048;
        String sndWdString = System.getProperty("sndWd");
        if (!StringUtil.isEmpty(sndWdString)) {
            sndWd = Integer.parseInt(sndWdString);
        }
        int rcvWd = 8096;
        String rcvWdString = System.getProperty("rcvWd");
        if (!StringUtil.isEmpty(rcvWdString)) {
            rcvWd = Integer.parseInt(rcvWdString);
        }

        bootstrap.childOption(ChannelOption.SO_SNDBUF, sndWd);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, rcvWd);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;
    private Channel channel;

    public TestTcpServer(int port) {
        this.bootstrap = new ServerBootstrap();
        boolean epoll = Epoll.isAvailable();

        // handler
        this.bossGroup = epoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        this.workerGroup = epoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        this.init();

        Class<? extends ServerSocketChannel> serverSocketChannelClass = epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

        bootstrap.group(bossGroup, workerGroup).channel(serverSocketChannelClass);

        SimpleChannelInboundHandler<ByteBuf> handler = this;
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                pipeline.addLast(handler);
            }
        });
        try {
            this.channel = bootstrap.bind(new InetSocketAddress(port)).sync().channel();
            System.out.println("tcp server start bind:" + port + "...");
            System.out.println("====================================");
        } catch (Exception e) {
            e.printStackTrace();
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();
            throw new RuntimeException("error", e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("tcp connect:" + ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        ByteBuf scByteBuf = NetTest.readMessageAndBuildReplyInServer(byteBuf);
        if (scByteBuf == null) {
            return;
        }
        // 写数据
        ctx.channel().writeAndFlush(scByteBuf);
//        scByteBuf.release();
//        System.out.println("tcp [" + ukcp + "] server send:" + s2c);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("tcp [" + ctx.channel() + "] close");
    }
}
