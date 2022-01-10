package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据Channel管理连接
 * <p>仅适用于只有TCP连接的KCP网络</p>
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:28
 **/
public class ServerChannelManager extends AbstractChannelManager {

    private Map<Channel, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp getKcp(Channel channel, ByteBuf readByteBuf, InetSocketAddress address) {
        return ukcpMap.get(channel);
    }

    @Override
    public void addKcp(Ukcp ukcp) {
        Channel channel = ukcp.user().getChannel();
        ukcpMap.put(channel, ukcp);
    }

    @Override
    public void remove(Ukcp ukcp) {
        Channel channel = ukcp.user().getChannel();
        ukcpMap.remove(channel);
    }

    @Override
    public Collection<Ukcp> getAll() {
        return ukcpMap.values();
    }
}
