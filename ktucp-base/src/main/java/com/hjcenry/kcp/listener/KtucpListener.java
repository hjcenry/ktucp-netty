package com.hjcenry.kcp.listener;

import com.hjcenry.kcp.Uktucp;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public interface KtucpListener {

    /**
     * 连接之后
     *
     * @param netId  连接网络id
     * @param uktucp KCP对象
     */
    void onConnected(int netId, Uktucp uktucp);

    /**
     * kcp message
     *
     * @param object 接受对象
     * @param uktucp KTUCP对象
     */
    void handleReceive(Object object, Uktucp uktucp) throws Exception;

    /**
     * kcp异常，之后此kcp就会被关闭
     *
     * @param ex     异常
     * @param uktucp 发生异常的kcp，null表示非kcp错误
     */
    void handleException(Throwable ex, Uktucp uktucp);

    /**
     * 关闭
     *
     * @param uktucp KTUCP对象
     */
    void handleClose(Uktucp uktucp);

    /**
     * 闲置超时
     *
     * @param uktucp kcp对象
     */
    void handleIdleTimeout(Uktucp uktucp);
}
