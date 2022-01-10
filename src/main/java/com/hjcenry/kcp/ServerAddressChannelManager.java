package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JinMiao
 * 2019/10/17.
 */
public class ServerAddressChannelManager extends AbstractChannelManager {

    private Map<SocketAddress, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp getKcp(Channel channel, ByteBuf object, InetSocketAddress address) {
        return ukcpMap.get(address);
    }

    @Override
    public void addKcp(Ukcp ukcp) {
        InetSocketAddress socketAddress = ukcp.user().getRemoteAddress();
        ukcpMap.put(socketAddress, ukcp);
    }

    @Override
    public void remove(Ukcp ukcp) {
        ukcpMap.remove(ukcp.user().getRemoteAddress());
    }

    @Override
    public Collection<Ukcp> getAll() {
        return this.ukcpMap.values();
    }
}
