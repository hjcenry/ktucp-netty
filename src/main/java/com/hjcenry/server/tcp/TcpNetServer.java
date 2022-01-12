package com.hjcenry.server.tcp;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Crc32Decode;
import com.hjcenry.kcp.Crc32Encode;
import com.hjcenry.server.AbstractNetServer;
import com.hjcenry.server.NetChannelConfig;
import com.hjcenry.server.NetConfigData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ServerChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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

    public TcpNetServer(int netId, NetConfigData netConfigData) throws KcpInitException {
        super(netId, netConfigData);
        serverChannelHandler = new TcpServerChannelHandler(this.netId,
                netConfigData.getChannelManager(),
                netConfigData.getChannelConfig(),
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
        Class<? extends ServerChannel> channelClass = this.nettyGroupChannel.getTcpChannelClass();
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
            Crc32Encode crc32Encode = new Crc32Encode();
            Crc32Decode crc32Decode = new Crc32Decode();
            cp.addLast(crc32Encode);
            cp.addLast(crc32Decode);
        }
        TcpChannelConfig tcpChannelConfig = (TcpChannelConfig) this.netConfigData.getNetChannelConfig();
        cp.addLast(new IdleStateHandler(tcpChannelConfig.getReadIdleTime(), tcpChannelConfig.getWriteIdleTime(), tcpChannelConfig.getAllIdleTime(), TimeUnit.MILLISECONDS));
//        cp.addLast(new LengthFieldBasedFrameDecoder(NettyConstants.SERVER_TO_SERVER_MAX_FRAME_LENGTH, 0, 4, 0, 4));
        cp.addLast(serverChannelHandler);
    }
}
