package com.hjcenry.net;

import com.hjcenry.net.server.NetTypeEnum;
import com.hjcenry.net.callback.StartUpNettyServerCallBack;
import com.hjcenry.net.tcp.INettyChannelEvent;
import io.netty.channel.ChannelHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络配置
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 17:06
 **/
public class NetChannelConfig {

    /**
     * 网络类型
     */
    protected NetTypeEnum netTypeEnum;
    /**
     * 绑定端口
     */
    protected int bindPort = -1;

    protected NetChannelConfig() {
    }

    /**
     * BOSS线程数量
     */
    private int bossThreadNum;
    /**
     * IO线程数量
     */
    private int ioThreadNum;
    /**
     * 绑定成功回调
     */
    private StartUpNettyServerCallBack bindCallback;
    /**
     * 启动成功回调
     */
    private StartUpNettyServerCallBack activeCallback;
    /**
     * 通道事件触发
     */
    private INettyChannelEvent nettyEventTrigger;
    /**
     * 自定义Handler
     */
    private final List<ChannelHandler> customChannelHandlerList = new ArrayList<>();

    /**
     * 网络ID，需要>0，可不指定，由服务端工厂{@link com.hjcenry.net.server.NetServerFactory}或客户端工厂{@link com.hjcenry.net.client.NetClientFactory}分配
     */
    private int netId;

    // 客户端参数

    /**
     * 本地address
     */
    protected InetSocketAddress clientConnectLocalAddress;
    /**
     * 远端address
     */
    protected InetSocketAddress clientConnectRemoteAddress;

    /**
     * 添加自定义handler
     *
     * @param channelHandler 自定义handler
     */
    public void addChannelHandler(ChannelHandler channelHandler) {
        this.customChannelHandlerList.add(channelHandler);
    }

    public List<ChannelHandler> getCustomChannelHandlerList() {
        return customChannelHandlerList;
    }

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public int getIoThreadNum() {
        return ioThreadNum;
    }

    public void setIoThreadNum(int ioThreadNum) {
        this.ioThreadNum = ioThreadNum;
    }

    public int getBindPort() {
        return bindPort;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public INettyChannelEvent getNettyEventTrigger() {
        return nettyEventTrigger;
    }

    public void setNettyEventTrigger(INettyChannelEvent nettyEventTrigger) {
        this.nettyEventTrigger = nettyEventTrigger;
    }

    /**
     * 是否使用当前网络
     *
     * @return 是否使用当前网络
     */
    public boolean isUsed() {
        return this.bindPort != -1;
    }

    public StartUpNettyServerCallBack getBindCallback() {
        return bindCallback;
    }

    public void setBindCallback(StartUpNettyServerCallBack bindCallback) {
        this.bindCallback = bindCallback;
    }

    public StartUpNettyServerCallBack getActiveCallback() {
        return activeCallback;
    }

    public void setActiveCallback(StartUpNettyServerCallBack activeCallback) {
        this.activeCallback = activeCallback;
    }

    public NetTypeEnum getNetTypeEnum() {
        return netTypeEnum;
    }

    public InetSocketAddress getClientConnectLocalAddress() {
        return clientConnectLocalAddress;
    }

    public InetSocketAddress getClientConnectRemoteAddress() {
        return clientConnectRemoteAddress;
    }

    public int getNetId() {
        return netId;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }
}
