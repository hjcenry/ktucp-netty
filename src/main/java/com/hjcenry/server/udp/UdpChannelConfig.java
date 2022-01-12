package com.hjcenry.server.udp;

import com.hjcenry.server.NetChannelConfig;
import com.hjcenry.server.NetServerEnum;
import com.hjcenry.server.callback.StartUpNettyServerCallBack;
import io.netty.channel.ChannelOption;

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

    public UdpChannelConfig(int bindPort) {
        super(NetServerEnum.NET_UDP, bindPort);
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
