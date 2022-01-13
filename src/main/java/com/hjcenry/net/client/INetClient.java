package com.hjcenry.net.client;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.kcp.Ukcp;
import com.hjcenry.net.INet;

/**
 * 网络服务接口
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 14:57
 **/
public interface INetClient extends INet {

    /**
     * 连接服务端
     *
     * @param ukcp KCP对象
     * @throws KcpInitException KCP初始化异常
     */
    public void connect(Ukcp ukcp) throws KcpInitException;

    /**
     * 重连客户端
     *
     * @param ukcp KCP对象
     * @throws KcpInitException KCP初始化异常
     */
    public void reconnect(Ukcp ukcp) throws KcpInitException;
}
