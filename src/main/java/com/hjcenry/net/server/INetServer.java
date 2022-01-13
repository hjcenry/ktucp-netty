package com.hjcenry.net.server;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.User;
import com.hjcenry.net.INet;
import com.hjcenry.net.client.INetClient;
import io.netty.buffer.ByteBuf;

/**
 * 网络服务接口
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 14:57
 **/
public interface INetServer extends INet {

    /**
     * 启动服务
     *
     * @throws KcpInitException KCP初始化异常
     */
    public void start() throws KcpInitException;
}
