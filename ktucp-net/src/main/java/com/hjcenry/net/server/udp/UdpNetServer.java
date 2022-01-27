package com.hjcenry.net.server.udp;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.net.server.AbstractServerChannelHandler;
import com.hjcenry.kcp.Crc32Decode;
import com.hjcenry.kcp.Crc32Encode;
import com.hjcenry.kcp.User;
import com.hjcenry.net.AbstractNet;
import com.hjcenry.net.server.AbstractNetServer;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.kcp.NetTypeEnum;
import com.hjcenry.net.udp.UdpChannelConfig;
import com.hjcenry.net.udp.UdpCrc32Decode;
import com.hjcenry.net.udp.UdpCrc32Encode;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramPacket;

import java.util.Map;

/**
 * UDP网络服务
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:02
 **/
public class UdpNetServer extends AbstractNetServer {

    private final AbstractServerChannelHandler serverChannelHandler;

    public UdpNetServer(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        super(netId, netTypeEnum, netConfigData);
        serverChannelHandler = new UdpServerChannelHandler(netId,
                netConfigData.getChannelManager(),
                netConfigData.getChannelConfig(),
                netConfigData.getNetChannelConfig(),
                netConfigData.getMessageExecutorPool(),
                netConfigData.getListener(),
                netConfigData.getHashedWheelTimer(),
                netConfigData.getMessageEncoder(),
                netConfigData.getMessageDecoder());
    }

    @Override
    protected void initConfigureBootStrap() {
        Bootstrap bootstrap = new Bootstrap();
        // 服务端Channel
        Class<? extends Channel> channelClass = this.nettyGroupChannel.getUdpChannelClass();
        bootstrap.channel(channelClass);
        bootstrap.group(bossGroup);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                UdpNetServer.this.initChannel(ch);
            }
        });
        this.bootstrap = bootstrap;
    }

    @Override
    protected void applyConnectionOptions() {
        Bootstrap bootstrap = (Bootstrap) this.bootstrap;
        // 默认配置ServerSocketChannel
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        // 自定义配置参数
        UdpChannelConfig udpChannelConfig = (UdpChannelConfig) this.netConfigData.getNetChannelConfig();
        for (Map.Entry<ChannelOption, Object> entry : udpChannelConfig.getChannelOptions().entrySet()) {
            ChannelOption channelOption = entry.getKey();
            Object value = entry.getValue();
            bootstrap.option(channelOption, value);
        }
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline cp = ch.pipeline();
        if (netConfigData.getChannelConfig().isCrc32Check()) {
            Crc32Encode crc32Encode = new UdpCrc32Encode();
            Crc32Decode crc32Decode = new UdpCrc32Decode();
            cp.addLast(crc32Encode);
            cp.addLast(crc32Decode);
        }

        UdpChannelConfig udpChannelConfig = (UdpChannelConfig) this.netConfigData.getNetChannelConfig();

        // 支持添加自定义处理器
        for (ChannelHandler channelHandler : udpChannelConfig.getCustomChannelHandlerList()) {
            if (channelHandler == null) {
                continue;
            }
            cp.addLast(channelHandler);
        }

        cp.addLast(serverChannelHandler);
    }

    @Override
    public void send(ByteBuf data, User user) {
        Channel channel = user.getNetChannel(this.netId);
        if (channel == null) {
            if (AbstractNet.logger.isWarnEnabled()) {
                AbstractNet.logger.warn(String.format("KcpOutput writeAndFlush net[%d] error : channel null", this.netId));
            }
            return;
        }
        // UDP写数据包
        DatagramPacket msg = new DatagramPacket(data, user.getNetRemoteAddress(this.netId), user.getNetLocalAddress(this.netId));
        channel.writeAndFlush(msg);
    }
}
