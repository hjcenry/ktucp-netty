package com.hjcenry.server.udp;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.IKcp;
import com.hjcenry.kcp.KcpOutPutImp;
import com.hjcenry.kcp.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

/**
 * UDP写数据
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 15:36
 **/
public class UdpOutPutImp extends KcpOutPutImp {

    @Override
    protected void writeAndFlush(ByteBuf data, User user) {
        DatagramPacket temp = new DatagramPacket(data, user.getRemoteAddress(), user.getLocalAddress());
        user.getChannel().writeAndFlush(temp);
    }
}
