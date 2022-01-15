package com.hjcenry.kcp;

import com.hjcenry.net.INet;
import com.hjcenry.net.server.NetTypeEnum;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户网络管理
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/14 10:25
 **/
public class UserNetManager {

    /**
     * Channel map
     */
    private final Map<Integer, UserNetInfo> netInfoMap;
    /**
     * 用户对象
     */
    private final User user;

    public UserNetManager(User user, int netNum) {
        this.user = user;
        this.netInfoMap = new ConcurrentHashMap<>(netNum);
    }

    /**
     * 是否拥有网络信息
     *
     * @param netId 网络id
     * @return 是否拥有网络信息
     */
    public boolean containsNet(int netId) {
        return this.netInfoMap.containsKey(netId);
    }

    /**
     * 添加网络信息
     * <b>适用于多通道网络</b>
     *
     * @param netId         网络id
     * @param channel       通道
     * @param localAddress  本地地址
     * @param remoteAddress 远端地址
     */
    public void addNetInfo(int netId, Channel channel, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        UserNetInfo userNetInfo = new UserNetInfo();
        userNetInfo.setNetId(netId);
        userNetInfo.setChannel(channel);
        userNetInfo.setRemoteAddress(remoteAddress);
        userNetInfo.setLocalAddress(localAddress);
        this.netInfoMap.put(netId, userNetInfo);
    }

    /**
     * 添加网络信息
     * <b>适用于单通道网络</b>
     *
     * @param channel       通道
     * @param localAddress  本地地址
     * @param remoteAddress 远端地址
     */
    public void addNetInfo(Channel channel, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        this.addNetInfo(INet.DEFAULT_CHANNEL_NET_ID, channel, localAddress, remoteAddress);
    }

    /**
     * 是否是UDP网络
     *
     * @param netId 网络
     * @return 是否是UDP网络
     */
    private boolean isUdpChannel(int netId) {
        INet net = KtucpNetManager.getNet(netId);
        if (net == null) {
            return false;
        }
        return net.getNetTypeEnum() == NetTypeEnum.NET_UDP;
    }

    /**
     * 关闭所有channel
     */
    public void closeAllChannel() {
        for (UserNetInfo userNetInfo : this.netInfoMap.values()) {
            int netId = userNetInfo.getNetId();
            INet net = KtucpNetManager.getNet(netId);
            if (net == null) {
                continue;
            }
            Channel channel = userNetInfo.getChannel();
            if (isUdpChannel(netId)) {
                if (this.user.isClient()) {
                    // UDP包，仅客户端关闭
                    channel.close();
                }
            } else {
                channel.close();
            }
        }
    }

    /**
     * 关闭网络
     * <b>适用于单通道网络</b>
     */
    public void closeNet() {
        Iterator<UserNetInfo> iterator = this.netInfoMap.values().iterator();
        if (!iterator.hasNext()) {
            return;
        }
        UserNetInfo userNetInfo = iterator.next();
        this.closeNet(userNetInfo.getNetId());
    }

    /**
     * 关闭channel
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     */
    public void closeNet(int netId) {
        UserNetInfo userNetInfo = this.netInfoMap.remove(netId);
        if (userNetInfo != null) {
            userNetInfo.getChannel().close();
        }
    }

    //===================================================================
    // Channel
    //===================================================================

    /**
     * 获取当前网络通道Channel
     *
     * @return 当前网络通道Channel
     */
    public Channel getCurrentNetChannel() {
        return getChannel(this.user.getCurrentNetId());
    }

    /**
     * 获取channel
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     * @return channel
     */
    public Channel getChannel(int netId) {
        if (!this.netInfoMap.containsKey(netId)) {
            return null;
        }
        UserNetInfo userNetInfo = this.netInfoMap.get(netId);
        return userNetInfo.getChannel();
    }

    /**
     * 获取channel
     * <b>适用于单通道网络</b>
     *
     * @return channel
     */
    public Channel getChannel() {
        Iterator<UserNetInfo> iterator = this.netInfoMap.values().iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        UserNetInfo userNetInfo = iterator.next();
        return userNetInfo.getChannel();
    }

    //===================================================================
    // 远端地址
    //===================================================================

    /**
     * 获取当前网络通道远端地址
     *
     * @return 当前网络通道远端地址
     */
    public InetSocketAddress getCurrentNetRemoteSocketAddress() {
        return this.getRemoteSocketAddress(this.user.getCurrentNetId());
    }

    /**
     * 获取远端地址
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     * @return 远端地址
     */
    public InetSocketAddress getRemoteSocketAddress(int netId) {
        if (!this.netInfoMap.containsKey(netId)) {
            return null;
        }
        UserNetInfo userNetInfo = this.netInfoMap.get(netId);
        return userNetInfo.getRemoteAddress();
    }

    /**
     * 获取远端地址
     * <b>适用于单通道网络</b>
     *
     * @return 远端地址
     */
    public InetSocketAddress getRemoteSocketAddress() {
        Iterator<UserNetInfo> iterator = this.netInfoMap.values().iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        UserNetInfo userNetInfo = iterator.next();
        return userNetInfo.getRemoteAddress();
    }

    /**
     * 设置远端地址
     *
     * @param netId               网络id
     * @param remoteSocketAddress 远端地址
     */
    public void setRemoteSocketAddress(int netId, InetSocketAddress remoteSocketAddress) {
        if (!this.netInfoMap.containsKey(netId)) {
            return;
        }
        UserNetInfo userNetInfo = this.netInfoMap.get(netId);
        userNetInfo.setRemoteAddress(remoteSocketAddress);
    }

    //===================================================================
    // 本地地址
    //===================================================================

    /**
     * 获取当前网络通道本地地址
     *
     * @return 当前网络通道本地地址
     */
    public InetSocketAddress getCurrentNetLocalSocketAddress() {
        return this.getLocalSocketAddress(this.user.getCurrentNetId());
    }

    /**
     * 获取本地地址
     * <b>适用于多通道网络</b>
     *
     * @param netId 网络id
     * @return 本地地址
     */
    public InetSocketAddress getLocalSocketAddress(int netId) {
        if (!this.netInfoMap.containsKey(netId)) {
            return null;
        }
        UserNetInfo userNetInfo = this.netInfoMap.get(netId);
        return userNetInfo.getLocalAddress();
    }

    /**
     * 获取本地地址
     * <b>适用于单通道网络</b>
     *
     * @return 本地地址
     */
    public InetSocketAddress getLocalSocketAddress() {
        Iterator<UserNetInfo> iterator = this.netInfoMap.values().iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        UserNetInfo userNetInfo = iterator.next();
        return userNetInfo.getLocalAddress();
    }

}
