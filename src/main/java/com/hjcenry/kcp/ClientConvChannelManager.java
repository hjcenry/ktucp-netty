package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据conv确定一个session
 * Created by JinMiao
 * 2019/10/17.
 */
public class ClientConvChannelManager extends AbstractChannelManager {

    public ClientConvChannelManager(int convIndex) {
        this.convIndex = convIndex;
    }

    private Map<Integer, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp getKcp(ByteBuf readByteBuf, InetSocketAddress address) {
        int conv = getConvIdByByteBuf(readByteBuf);
        return ukcpMap.get(conv);
    }

    @Override
    public void addKcp(Ukcp ukcp) {
        int conv = ukcp.getConv();
        ukcpMap.put(conv, ukcp);
    }

    @Override
    public void remove(Ukcp ukcp) {
        ukcpMap.remove(ukcp.getConv());
        ukcp.closeChannel();
    }

    @Override
    public Collection<Ukcp> getAll() {
        return this.ukcpMap.values();
    }
}
