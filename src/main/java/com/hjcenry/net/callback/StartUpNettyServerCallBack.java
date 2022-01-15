package com.hjcenry.net.callback;

import io.netty.util.concurrent.Future;

/**
 * Netty服务启动回调
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 11:47
 **/
public abstract class StartUpNettyServerCallBack {

    public void apply(int netId) {
        this.apply(null, netId);
    }

    /**
     * future可能传空，空代表非异步调用，可直接认为执行成功
     *
     * @param future
     * @param netId
     */
    public abstract void apply(Future<Void> future, int netId);


}
