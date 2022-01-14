package com.hjcenry.kcp;


import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地址管理通道管理器只适用于单网络通道
 * <p>
 * Created by JinMiao
 * 2019/10/16.
 */
public class ClientAddressChannelManager extends AbstractChannelManager {

    private final Map<SocketAddress, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp getKcp(ByteBuf byteBuf, InetSocketAddress address) {
        return ukcpMap.get(address);
    }

    @Override
    public void addKcp(Ukcp ukcp) {
        // 地址管理网络，只能是单通道网络
        InetSocketAddress localAddress = ukcp.user().getUserNetManager().getLocalSocketAddress();
        ukcpMap.put(localAddress, ukcp);
    }

    @Override
    public void remove(Ukcp ukcp) {
        // 地址管理网络，只能是单通道网络
        InetSocketAddress localAddress = ukcp.user().getUserNetManager().getLocalSocketAddress();
        ukcpMap.remove(localAddress);
        ukcp.closeChannel();
    }

    @Override
    public Collection<Ukcp> getAll() {
        return this.ukcpMap.values();
    }
}
