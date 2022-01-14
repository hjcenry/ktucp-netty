package com.hjcenry.kcp.listener;

import com.hjcenry.kcp.Ukcp;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public interface KtucpListener {

    /**
     * 连接之后
     *
     * @param netId 连接网络id
     * @param ukcp  KCP对象
     */
    void onConnected(int netId, Ukcp ukcp);

    /**
     * kcp message
     *
     * @param object
     * @param ukcp
     */
    void handleReceive(Object object, Ukcp ukcp) throws Exception;

    /**
     * kcp异常，之后此kcp就会被关闭
     *
     * @param ex   异常
     * @param ukcp 发生异常的kcp，null表示非kcp错误
     */
    void handleException(Throwable ex, Ukcp ukcp);

    /**
     * 关闭
     *
     * @param ukcp
     */
    void handleClose(Ukcp ukcp);

    /**
     * 闲置超时
     *
     * @param ukcp kcp对象
     */
    void handleIdleTimeout(Ukcp ukcp);
}
