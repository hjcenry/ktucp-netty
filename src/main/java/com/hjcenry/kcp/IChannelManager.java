package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

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
     * @param channel     通道
     * @param readByteBuf 消息对象
     * @param address
     * @return kcp对象
     */
    Ukcp getKcp(Channel channel, ByteBuf readByteBuf, InetSocketAddress address);

    /**
     * 创建KCP对象
     *
     * @param ukcp kcp对象
     */
    void addKcp(Ukcp ukcp);

    /**
     * 移除KCP对象
     *
     * @param ukcp KCP对象
     */
    void remove(Ukcp ukcp);

    /**
     * 获取所有KCP对象
     *
     * @return KCP对象集合
     */
    Collection<Ukcp> getAll();

    /**
     * 读取convId
     *
     * @param readByteBuf 读数据
     * @return convId
     */
    int getConvIdByByteBuf(ByteBuf readByteBuf);
}
