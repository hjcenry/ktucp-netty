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
 * 2019/10/17.
 */
public class ServerAddressChannelManager extends AbstractChannelManager {

    private final Map<SocketAddress, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp getKcp(ByteBuf object, InetSocketAddress address) {
        return ukcpMap.get(address);
    }

    @Override
    public void addKcp(Ukcp ukcp) {
        // 地址管理网络，只能是单通道网络
        InetSocketAddress remoteSocketAddress = ukcp.user().getUserNetManager().getRemoteSocketAddress();
        ukcpMap.put(remoteSocketAddress, ukcp);
    }

    @Override
    public void remove(Ukcp ukcp) {
        // 地址管理网络，只能是单通道网络
        InetSocketAddress remoteSocketAddress = ukcp.user().getUserNetManager().getRemoteSocketAddress();
        ukcpMap.remove(remoteSocketAddress);
    }

    @Override
    public Collection<Ukcp> getAll() {
        return this.ukcpMap.values();
    }
}
