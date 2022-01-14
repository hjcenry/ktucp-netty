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

    private final Map<SocketAddress, Uktucp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Uktucp getKcp(ByteBuf object, InetSocketAddress address) {
        return ukcpMap.get(address);
    }

    @Override
    public void addKcp(Uktucp uktucp) {
        // 地址管理网络，只能是单通道网络
        InetSocketAddress remoteSocketAddress = uktucp.user().getUserNetManager().getRemoteSocketAddress();
        ukcpMap.put(remoteSocketAddress, uktucp);
    }

    @Override
    public void remove(Uktucp uktucp) {
        // 地址管理网络，只能是单通道网络
        InetSocketAddress remoteSocketAddress = uktucp.user().getUserNetManager().getRemoteSocketAddress();
        ukcpMap.remove(remoteSocketAddress);
    }

    @Override
    public Collection<Uktucp> getAll() {
        return this.ukcpMap.values();
    }
}
