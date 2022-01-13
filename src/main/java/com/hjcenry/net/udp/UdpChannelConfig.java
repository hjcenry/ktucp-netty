package com.hjcenry.net.udp;

import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.server.NetTypeEnum;
import io.netty.channel.ChannelOption;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * UDP网络配置
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 17:06
 **/
public class UdpChannelConfig extends NetChannelConfig {
    /**
     * UDP Channel参数
     */
    private final Map<ChannelOption, Object> channelOptions = new HashMap<>();

    protected UdpChannelConfig() {
    }

    /**
     * 构建服务端配置
     *
     * @param bindPort 绑定端口
     * @return 服务端配置
     */
    public static UdpChannelConfig buildServerConfig(int bindPort) {
        UdpChannelConfig channelConfig = new UdpChannelConfig();
        channelConfig.netTypeEnum = NetTypeEnum.NET_UDP;
        channelConfig.bindPort = bindPort;
        return channelConfig;
    }

    /**
     * 构建客户端配置
     *
     * @param serverAddress 远端地址
     * @return 客户端配置
     */
    public static UdpChannelConfig buildClientConfig(InetSocketAddress serverAddress) {
        return buildClientConfig(null, serverAddress);
    }

    /**
     * 构建客户端配置
     *
     * @param localAddress  本地地址
     * @param serverAddress 远端地址
     * @return 客户端配置
     */
    public static UdpChannelConfig buildClientConfig(InetSocketAddress localAddress, InetSocketAddress serverAddress) {
        UdpChannelConfig channelConfig = new UdpChannelConfig();
        channelConfig.netTypeEnum = NetTypeEnum.NET_UDP;
        channelConfig.clientConnectLocalAddress = localAddress;
        channelConfig.clientConnectRemoteAddress = serverAddress;
        return channelConfig;
    }

    /**
     * 添加UDP Channel参数
     *
     * @param channelOption 参数key
     * @param t             参数值
     */
    public <T> void addUdpChannelOption(ChannelOption<T> channelOption, T t) {
        this.channelOptions.put(channelOption, t);
    }

    public Map<ChannelOption, Object> getChannelOptions() {
        return channelOptions;
    }
}
