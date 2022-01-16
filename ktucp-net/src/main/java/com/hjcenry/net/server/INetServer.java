package com.hjcenry.net.server;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.INet;

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
     * @throws KtucpInitException KCP初始化异常
     */
    public void start() throws KtucpInitException;
}
