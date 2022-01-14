package com.hjcenry.kcp;

import io.netty.channel.Channel;

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

    private Map<Channel, Uktucp> ukcpMap = new ConcurrentHashMap<>();

    public Uktucp getKcp(Channel channel) {
        return ukcpMap.get(channel);
    }

    public void addKcp(Uktucp uktucp, Channel channel) {
        ukcpMap.put(channel, uktucp);
    }

    public void remove(Channel channel) {
        ukcpMap.remove(channel);
    }
}
