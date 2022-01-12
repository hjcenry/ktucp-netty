package com.hjcenry.server;

import com.hjcenry.server.callback.StartUpNettyServerCallBack;
import com.hjcenry.server.tcp.INettyChannelEvent;

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
    private NetServerEnum serverEnum;
    /**
     * 绑定端口
     */
    private int bindPort = -1;

    public NetChannelConfig(NetServerEnum serverEnum, int bindPort) {
        this.serverEnum = serverEnum;
        this.bindPort = bindPort;
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
    private INettyChannelEvent eventTrigger;

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

    public INettyChannelEvent getEventTrigger() {
        return eventTrigger;
    }

    public void setEventTrigger(INettyChannelEvent eventTrigger) {
        this.eventTrigger = eventTrigger;
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

    public NetServerEnum getServerEnum() {
        return serverEnum;
    }
}
