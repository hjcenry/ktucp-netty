package com.hjcenry.kcp;

import com.hjcenry.net.INet;
import com.hjcenry.net.server.NetTypeEnum;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JinMiao
 * 2018/11/2.
 */
public class User {
    /**
     * 默认服务端模式
     */
    private boolean isClient = false;
    /**
     * 当前网络id
     */
    private volatile int currentNetId;
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
     * <p>使用默认网络id</p>
     *
     * @param remoteAddress 远程地址
     * @param localAddress  本地地址
     * @param netNum        网络数量
     */
    public User(InetSocketAddress remoteAddress, InetSocketAddress localAddress, int netNum) {
        this(INet.DEFAULT_CHANNEL_NET_ID, remoteAddress, localAddress, netNum);
    }

    /**
     * 网络数量
     * <p>
     * 未指定adress和netId，仅做初始化
     * </p>
     *
     * @param netNum 网络数量
     */
    public User(int netNum) {
        this(INet.DEFAULT_CHANNEL_NET_ID, null, null, netNum);
    }

    /**
     * 创建User对象
     *
     * @param currentNetId  当前网络id
     * @param remoteAddress 远程地址
     * @param localAddress  本地地址
     * @param netNum        网络数量
     */
    public User(int currentNetId, InetSocketAddress remoteAddress, InetSocketAddress localAddress, int netNum) {
        this.currentNetId = currentNetId;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.channelMap = new ConcurrentHashMap<>(netNum);
    }

    protected void setClient(boolean client) {
        isClient = client;
    }

    /**
     * 获取当前网络通道Channel
     *
     * @return 当前网络通道Channel
     */
    public Channel getCurrentNetChannel() {
        if (!this.channelMap.containsKey(this.currentNetId)) {
            return null;
        }
        return this.channelMap.get(this.currentNetId);
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
        this.channelMap.put(INet.DEFAULT_CHANNEL_NET_ID, channel);
    }

    /**
     * 获取channel
     * <b>适用于单通道网络</b>
     *
     * @return channel
     */
    public Channel getChannel() {
        return this.channelMap.get(INet.DEFAULT_CHANNEL_NET_ID);
    }

    /**
     * 是否是UDP网络
     *
     * @param netId 网络
     * @return 是否是UDP网络
     */
    private boolean isUdpChannel(int netId) {
        INet net = KcpNetManager.getNet(netId);
        if (net == null) {
            return false;
        }
        return net.getNetTypeEnum() == NetTypeEnum.NET_UDP;
    }

    /**
     * 关闭所有channel
     */
    public void closeAllChannel() {
        for (Map.Entry<Integer, Channel> entry : this.channelMap.entrySet()) {
            int netId = entry.getKey();
            INet net = KcpNetManager.getNet(netId);
            if (net == null) {
                continue;
            }
            Channel channel = entry.getValue();

            if (isUdpChannel(netId)) {
                if (isClient) {
                    // UDP包，仅客户端关闭
                    channel.close();
                }
            } else {
                channel.close();
            }
        }
    }

    /**
     * 关闭channel
     * <b>适用于单通道网络</b>
     */
    public void closeChannel() {
        this.closeChannel(INet.DEFAULT_CHANNEL_NET_ID);
    }

    /**
     * 关闭channel
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     */
    public void closeChannel(int netId) {
        Channel channel = this.channelMap.remove(netId);
        channel.close();
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

    public int getCurrentNetId() {
        return currentNetId;
    }

    protected void setCurrentNetId(int currentNetId) {
        this.currentNetId = currentNetId;
    }

    public boolean isClient() {
        return isClient;
    }

    @Override
    public String toString() {
        return "User{" +
                "remoteAddress=" + remoteAddress +
                ", localAddress=" + localAddress +
                '}';
    }
}
