package com.hjcenry.net.server.tcp;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.kcp.Crc32Decode;
import com.hjcenry.kcp.Crc32Encode;
import com.hjcenry.kcp.User;
import com.hjcenry.net.server.AbstractNetServer;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.server.NetTypeEnum;
import com.hjcenry.net.tcp.TcpChannelConfig;
import com.hjcenry.net.tcp.TcpCrc32Decode;
import com.hjcenry.net.tcp.TcpCrc32Encode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ServerChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TCP网络服务
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:02
 **/
public class TcpNetServer extends AbstractNetServer {

    private final AbstractServerChannelHandler serverChannelHandler;

    public TcpNetServer(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        super(netId, netTypeEnum, netConfigData);
        serverChannelHandler = new TcpServerChannelHandler(netId,
                netConfigData.getChannelManager(),
                netConfigData.getChannelConfig(),
                netConfigData.getNetChannelConfig(),
                netConfigData.getMessageExecutorPool(),
                netConfigData.getListener(),
                netConfigData.getHashedWheelTimer(),
                netConfigData.getMessageEncoder(),
                netConfigData.getMessageDecoder()
        );
    }

    @Override
    protected void initConfigureBootStrap() {
        // 服务端Channel
        Class<? extends ServerChannel> channelClass = this.nettyGroupChannel.getTcpServerChannelClass();
        // 创建服务端启动器
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 配置启动器
        serverBootstrap.group(bossGroup, ioGroup)
                .channel(channelClass)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        TcpNetServer.this.initChannel(ch);
                    }
                });
        this.bootstrap = serverBootstrap;
    }

    @Override
    protected void applyConnectionOptions() {
        ServerBootstrap serverBootstrap = (ServerBootstrap) this.bootstrap;
        TcpChannelConfig tcpChannelConfig = (TcpChannelConfig) this.netConfigData.getNetChannelConfig();

        // 默认配置ServerSocketChannel
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 100);
        serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        // 自定义配置参数
        for (Map.Entry<ChannelOption, Object> entry : tcpChannelConfig.getServerChannelOptions().entrySet()) {
            ChannelOption channelOption = entry.getKey();
            Object value = entry.getValue();
            serverBootstrap.option(channelOption, value);
        }

        // 默认配置SocketChannel
        // 重用端口
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        // TCP探活
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 禁用Negal算法保证立即发送数据包
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        // 自定义配置参数
        for (Map.Entry<ChannelOption, Object> entry : tcpChannelConfig.getChildChannelOptions().entrySet()) {
            ChannelOption channelOption = entry.getKey();
            Object value = entry.getValue();
            serverBootstrap.option(channelOption, value);
        }
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline cp = ch.pipeline();
        if (netConfigData.getChannelConfig().isCrc32Check()) {
            Crc32Encode crc32Encode = new TcpCrc32Encode();
            Crc32Decode crc32Decode = new TcpCrc32Decode();
            cp.addLast(crc32Encode);
            cp.addLast(crc32Decode);
        }

        TcpChannelConfig tcpChannelConfig = (TcpChannelConfig) this.netConfigData.getNetChannelConfig();

        // 超时时间
        long readIdleTime = tcpChannelConfig.getReadIdleTime();
        long writeIdleTime = tcpChannelConfig.getWriteIdleTime();
        long allIdleTime = tcpChannelConfig.getAllIdleTime();
        if (readIdleTime > 0 || writeIdleTime > 0 || allIdleTime > 0) {
            cp.addLast(new IdleStateHandler(tcpChannelConfig.getReadIdleTime(), tcpChannelConfig.getWriteIdleTime(), tcpChannelConfig.getAllIdleTime(), TimeUnit.MILLISECONDS));
        }

        if (tcpChannelConfig.getMaxFrameLength() > 0) {
            // 使用提供的粘包拆包编解码处理器

            // 粘包拆包解码器
            ByteToMessageDecoder frameDecoder = new LengthFieldBasedFrameDecoder(tcpChannelConfig.getMaxFrameLength(), tcpChannelConfig.getLengthFieldOffset(), tcpChannelConfig.getLengthFieldLength(), tcpChannelConfig.getLengthAdjustment(), 4);
            cp.addLast(frameDecoder);
            // 粘包拆包编码器
            MessageToMessageEncoder<ByteBuf> frameEncoder = new LengthFieldPrepender(tcpChannelConfig.getLengthFieldLength(), tcpChannelConfig.getLengthAdjustment(), false);
            cp.addLast(frameEncoder);
        }

        // 支持添加自定义处理器
        for (ChannelHandler channelHandler : tcpChannelConfig.getCustomChannelHandlerList()) {
            if (channelHandler == null) {
                continue;
            }
            cp.addLast(channelHandler);
        }

        cp.addLast(serverChannelHandler);
    }

    @Override
    public void send(ByteBuf data, User user) {
        Channel channel = user.getCurrentNetChannel();
        if (channel == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("KcpOutput writeAndFlush currentNet[%d] error : channel null", user.getCurrentNetId()));
            }
            return;
        }
        // TCP写字节流
        channel.writeAndFlush(data);
    }
}
