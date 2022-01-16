package com.hjcenry.net.client;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.INet;

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
     * @param uktucp KCP对象
     * @throws KtucpInitException KCP初始化异常
     */
    public void connect(Uktucp uktucp) throws KtucpInitException;

    /**
     * 重连客户端
     *
     * @param uktucp KCP对象
     * @throws KtucpInitException KCP初始化异常
     */
    public void reconnect(Uktucp uktucp) throws KtucpInitException;
}
