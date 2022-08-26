package com.hjcenry.kcp;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * 用户网络信息
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/14 10:30
 **/
public class UserNetInfo {
    /**
     * 网络id
     */
    private INet net;
    /**
     * 网络通道
     */
    private Channel channel;
    /**
     * 远端地址
     */
    private InetSocketAddress remoteAddress;
    /**
     * 本地地址
     */
    private InetSocketAddress localAddress;

    public INet getNet() {
        return net;
    }

    public void setNet(INet net) {
        this.net = net;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }
}
