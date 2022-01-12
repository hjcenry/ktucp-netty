package com.hjcenry.kcp;

import com.hjcenry.server.INetServer;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JinMiao
 * 2018/11/2.
 */
public class User {

    /**
     * Channel map
     */
    private final Map<Integer, Channel> channelMap;

    private InetSocketAddress remoteAddress;
    private InetSocketAddress localAddress;

    private Object cache;

    public void setCache(Object cache) {
        this.cache = cache;
    }

    public <T> T getCache() {
        return (T) cache;
    }

    /**
     * 创建User对象
     *
     * @param remoteAddress 远程地址
     * @param localAddress  本地地址
     * @param netNum        网络数量
     */
    public User(InetSocketAddress remoteAddress, InetSocketAddress localAddress, int netNum) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.channelMap = new ConcurrentHashMap<>(netNum);
    }

    /**
     * 获取channel
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     * @return channel
     */
    public Channel getChannel(int netId) {
        return this.channelMap.get(netId);
    }

    /**
     * 添加channel
     * <b>适用于多通道网络</b>
     *
     * @param netId   网络id
     * @param channel 通道
     */
    public void addChannel(int netId, Channel channel) {
        this.channelMap.put(netId, channel);
    }

    /**
     * 添加channel
     * <b>适用于单通道网络</b>
     *
     * @param channel 通道
     */
    public void addChannel(Channel channel) {
        this.channelMap.put(INetServer.DEFAULT_CHANNEL_NET_ID, channel);
    }

    /**
     * 获取channel
     * <b>适用于单通道网络</b>
     *
     * @return channel
     */
    public Channel getChannel() {
        return this.channelMap.get(INetServer.DEFAULT_CHANNEL_NET_ID);
    }

    /**
     * 关闭所有channel
     */
    public void closeAllChannel() {
        for (Channel channel : this.channelMap.values()) {
            channel.close();
        }
    }

    /**
     * 关闭channel
     * <b>适用于单通道网络</b>
     */
    public void closeChannel() {
        this.channelMap.remove(INetServer.DEFAULT_CHANNEL_NET_ID);
    }

    /**
     * 关闭channel
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     */
    public void closeChannel(int netId) {
        this.channelMap.remove(netId);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    protected void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    protected void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }


    @Override
    public String toString() {
        return "User{" +
                "remoteAddress=" + remoteAddress +
                ", localAddress=" + localAddress +
                '}';
    }
}
