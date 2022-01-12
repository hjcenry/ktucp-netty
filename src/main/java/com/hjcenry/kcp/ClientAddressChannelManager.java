package com.hjcenry.kcp;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JinMiao
 * 2019/10/16.
 */
public class ClientAddressChannelManager extends AbstractChannelManager {

    private Map<SocketAddress, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp getKcp(Channel channel, ByteBuf byteBuf, InetSocketAddress address) {
        return ukcpMap.get(address);
    }

    @Override
    public void addKcp(Ukcp ukcp, Channel channel) {
        InetSocketAddress localAddress = ukcp.user().getLocalAddress();
        ukcpMap.put(localAddress, ukcp);
    }

    @Override
    public void remove(Ukcp ukcp) {
        ukcpMap.remove(ukcp.user().getLocalAddress());
    }

    @Override
    public Collection<Ukcp> getAll() {
        return this.ukcpMap.values();
    }
}
