package com.hjcenry.server;

import com.hjcenry.exception.KcpInitException;

/**
 * 网络服务器接口
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 14:57
 **/
public interface INetServer {

    /**
     * 默认网络ID，用于单通道网络
     */
    public static final int DEFAULT_CHANNEL_NET_ID = 0;

    /**
     * 启动服务
     *
     * @throws KcpInitException KCP初始化异常
     */
    public void start() throws KcpInitException;

    /**
     * 停止服务
     */
    public void stop();

}
