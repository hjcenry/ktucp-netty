package com.hjcenry.server.tcp;

import com.hjcenry.server.NetChannelConfig;
import com.hjcenry.server.callback.StartUpNettyServerCallBack;
import io.netty.channel.ChannelOption;

import java.util.HashMap;
import java.util.Map;

/**
 * TCP网络配置
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 17:06
 **/
public class TcpChannelConfig extends NetChannelConfig {
    /**
     * TCP服务Channel参数
     */
    private final Map<ChannelOption, Object> serverChannelOptions = new HashMap<>();
    /**
     * TCP客户端Channel参数
     */
    private final Map<ChannelOption, Object> childChannelOptions = new HashMap<>();

    /**
     * 添加服务端Channel配置
     *
     * @param channelOption 参数Key
     * @param t             参数值
     */
    public <T> void addTcpServerChannelOption(ChannelOption<T> channelOption, T t) {
        this.serverChannelOptions.put(channelOption, t);
    }

    /**
     * 添加Child端Channel配置
     *
     * @param channelOption 参数Key
     * @param t             参数值
     */
    public <T> void addTcpChildChannelOption(ChannelOption<T> channelOption, T t) {
        this.childChannelOptions.put(channelOption, t);
    }

    public Map<ChannelOption, Object> getServerChannelOptions() {
        return serverChannelOptions;
    }

    public Map<ChannelOption, Object> getChildChannelOptions() {
        return childChannelOptions;
    }
}
