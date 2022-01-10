package com.hjcenry.server;

import com.hjcenry.server.callback.StartUpNettyServerCallBack;
import io.netty.channel.ChannelOption;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络配置
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 17:06
 **/
public class NetChannelConfig {

    /**
     * BOSS线程数量
     */
    private int bossThreadNum;
    /**
     * IO线程数量
     */
    private int ioThreadNum;
    /**
     * 绑定端口
     */
    private int bindPort = -1;
    /**
     * 绑定成功回调
     */
    private StartUpNettyServerCallBack bindSuccessCallback;
    /**
     * 启动成功回调
     */
    private StartUpNettyServerCallBack activeSuccessCallback;

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

    /**
     * 是否使用当前网络
     *
     * @return 是否使用当前网络
     */
    public boolean isUsed() {
        return this.bindPort != -1;
    }

    public StartUpNettyServerCallBack getBindSuccessCallback() {
        return bindSuccessCallback;
    }

    public void setBindSuccessCallback(StartUpNettyServerCallBack bindSuccessCallback) {
        this.bindSuccessCallback = bindSuccessCallback;
    }

    public StartUpNettyServerCallBack getActiveSuccessCallback() {
        return activeSuccessCallback;
    }

    public void setActiveSuccessCallback(StartUpNettyServerCallBack activeSuccessCallback) {
        this.activeSuccessCallback = activeSuccessCallback;
    }
}
