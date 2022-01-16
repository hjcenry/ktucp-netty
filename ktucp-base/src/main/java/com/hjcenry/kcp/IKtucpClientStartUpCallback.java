package com.hjcenry.kcp;


import com.hjcenry.kcp.Uktucp;

/**
 * 启动完成回调
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/14 18:56
 **/
public interface IKtucpClientStartUpCallback {

    /**
     * 回调
     *
     * @param uktucp
     */
    public void apply(Uktucp uktucp);
}
