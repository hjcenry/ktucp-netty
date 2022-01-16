package com.hjcenry.kcp;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

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
     * 强制使用网络id
     */
    private volatile int forceUseNetId = INet.NO_USE_FORCE_NET_ID;
    /**
     * 网络信息管理器
     */
    private final UserNetManager userNetManager;

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
     * @param netNum 网络数量
     */
    public User(int netNum) {
        this(INet.DEFAULT_CHANNEL_NET_ID, netNum);
    }

    /**
     * 创建User对象
     *
     * @param currentNetId 当前网络id
     * @param netNum       网络数量
     */
    public User(int currentNetId, int netNum) {
        this.currentNetId = currentNetId;
        this.userNetManager = new UserNetManager(this, netNum);
    }

    protected void setClient(boolean client) {
        isClient = client;
    }

    /**
     * 获取Channel
     *
     * @return Channel
     */
    public Channel getNetChannel(int netId) {
        return this.userNetManager.getChannel(netId);
    }

    /**
     * 获取通道远端地址
     *
     * @param netId 网络id
     * @return 远端地址
     */
    public InetSocketAddress getNetRemoteAddress(int netId) {
        return this.userNetManager.getRemoteSocketAddress(netId);
    }

    /**
     * 获取通道本地地址
     *
     * @param netId 网络id
     * @return 本地地址
     */
    public InetSocketAddress getNetLocalAddress(int netId) {
        return this.userNetManager.getLocalSocketAddress(netId);
    }

    /**
     * 获取当前网络通道Channel
     *
     * @return 当前网络通道Channel
     */
    public Channel getCurrentNetChannel() {
        return this.userNetManager.getCurrentNetChannel();
    }

    /**
     * 获取当前通道远端地址
     *
     * @return 远端地址
     */
    public InetSocketAddress getCurrentNetRemoteAddress() {
        return this.userNetManager.getCurrentNetRemoteSocketAddress();
    }

    /**
     * 获取当前通道本地地址
     *
     * @return 本地地址
     */
    public InetSocketAddress getCurrentNetLocalAddress() {
        return this.userNetManager.getCurrentNetLocalSocketAddress();
    }

    public void changeRemoteAddress(int netId, InetSocketAddress remoteAddress) {
        this.userNetManager.setRemoteSocketAddress(netId, remoteAddress);
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

    public UserNetManager getUserNetManager() {
        return userNetManager;
    }

    protected int getForceUseNetId() {
        return forceUseNetId;
    }

    protected void setForceUseNetId(int forceUseNetId) {
        this.forceUseNetId = forceUseNetId;
    }

    @Override
    public String toString() {
        return "User{" +
                "current net=" + getCurrentNetId() +
                ",current remoteAddress=" + getCurrentNetRemoteAddress() +
                ",current  localAddress=" + getCurrentNetLocalAddress() +
                '}';
    }
}
