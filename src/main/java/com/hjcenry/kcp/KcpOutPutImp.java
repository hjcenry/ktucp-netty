package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.server.INetServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by JinMiao
 * 2018/9/21.
 */
public class KcpOutPutImp implements KcpOutput {

    private final int netId;

    /**
     * 单通道网络构造方法
     */
    public KcpOutPutImp() {
        this.netId = INetServer.DEFAULT_CHANNEL_NET_ID;
    }

    /**
     * 多通道网络构造方法，需要指定网络id
     *
     * @param netId 网络id
     */
    public KcpOutPutImp(int netId) {
        this.netId = netId;
    }

    @Override
    public void out(ByteBuf data, IKcp kcp) {
        // 统计
        Snmp.snmp.OutPkts.increment();
        Snmp.snmp.OutBytes.add(data.writerIndex());
        // 回写数据
        User user = (User) kcp.getUser();
        this.writeAndFlush(data, user);
    }

    /**
     * 写数据
     *
     * @param data 数据
     * @param user 用户
     */
    protected void writeAndFlush(ByteBuf data, User user) {
        Channel channel = user.getChannel(netId);
        if (channel == null) {
            return;
        }
        channel.writeAndFlush(data);
    }
}
