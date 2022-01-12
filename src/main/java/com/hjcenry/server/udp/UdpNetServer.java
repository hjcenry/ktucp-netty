package com.hjcenry.server.udp;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.AbstractServerChannelHandler;
import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.kcp.Crc32Decode;
import com.hjcenry.kcp.Crc32Encode;
import com.hjcenry.server.AbstractNetServer;
import com.hjcenry.server.NetChannelConfig;
import com.hjcenry.server.NetConfigData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;

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

    public UdpNetServer(int netId, NetConfigData netConfigData) throws KcpInitException {
        super(netId, netConfigData);
        serverChannelHandler = new UdpServerChannelHandler(this.netId,
                netConfigData.getChannelManager(),
                netConfigData.getChannelConfig(),
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
        ChannelConfig channelConfig = this.netConfigData.getChannelConfig();
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
            Crc32Encode crc32Encode = new Crc32Encode();
            Crc32Decode crc32Decode = new Crc32Decode();
            cp.addLast(crc32Encode);
            cp.addLast(crc32Decode);
        }
        cp.addLast(serverChannelHandler);
    }
}
