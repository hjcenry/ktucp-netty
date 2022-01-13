package com.hjcenry.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel连接管理
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:28
 **/
public class ServerHandlerChannelManager {

    private Map<Channel, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    public Ukcp getKcp(Channel channel) {
        return ukcpMap.get(channel);
    }

    public void addKcp(Ukcp ukcp, Channel channel) {
        ukcpMap.put(channel, ukcp);
    }

    public void remove(Channel channel) {
        ukcpMap.remove(channel);
    }
}
