package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;

/**
 * 网络服务接口
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 14:57
 **/
public interface INet {

    /**
     * 默认网络ID，用于单通道网络
     */
    public static final int DEFAULT_CHANNEL_NET_ID = 0;
    /**
     * 不强制使用网络
     */
    public static final int NO_USE_FORCE_NET_ID = -1;

    /**
     * 停止连接
     */
    public void stop();

    /**
     * 获取网络id
     *
     * @return 网络id
     */
    public int getNetId();

    /**
     * 发送数据
     * <p>实现网络的writeAndFlush方法</p>
     *
     * @param data 数据
     * @param user 通道
     */
    public void send(ByteBuf data, User user);

    /**
     * 网络类型
     *
     * @return 网络类型
     */
    public NetTypeEnum getNetTypeEnum();
}
