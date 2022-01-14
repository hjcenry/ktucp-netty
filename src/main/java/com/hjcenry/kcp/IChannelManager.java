package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * KCP对象管理器
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 16:14
 **/
public interface IChannelManager {

    /**
     * 获取KCP对象
     *
     * @param readByteBuf 消息对象
     * @param address
     * @return kcp对象
     */
    Uktucp getKcp(ByteBuf readByteBuf, InetSocketAddress address);

    /**
     * 创建KCP对象
     *
     * @param uktucp kcp对象
     */
    void addKcp(Uktucp uktucp);

    /**
     * 移除KCP对象
     *
     * @param uktucp KCP对象
     */
    void remove(Uktucp uktucp);

    /**
     * 获取所有KCP对象
     *
     * @return KCP对象集合
     */
    Collection<Uktucp> getAll();

    /**
     * 读取convId
     *
     * @param readByteBuf 读数据
     * @return convId
     */
    int getConvIdByByteBuf(ByteBuf readByteBuf);
}
