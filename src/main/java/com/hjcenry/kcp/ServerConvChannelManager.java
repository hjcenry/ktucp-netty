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
public class ServerConvChannelManager extends AbstractChannelManager {

    public ServerConvChannelManager(int convIndex) {
        this.convIndex = convIndex;
    }

    private Map<Integer, Uktucp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Uktucp getKcp(ByteBuf readByteBuf, InetSocketAddress address) {
        int conv = getConvIdByByteBuf(readByteBuf);
        return ukcpMap.get(conv);
    }

    @Override
    public void addKcp(Uktucp uktucp) {
        int conv = uktucp.getConv();
        ukcpMap.put(conv, uktucp);
    }

    @Override
    public void remove(Uktucp uktucp) {
        ukcpMap.remove(uktucp.getConv());
    }

    @Override
    public Collection<Uktucp> getAll() {
        return this.ukcpMap.values();
    }
}
