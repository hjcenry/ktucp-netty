package com.hjcenry.server.tcp;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.IKcp;
import com.hjcenry.kcp.KcpOutPutImp;
import com.hjcenry.kcp.KcpOutput;
import com.hjcenry.kcp.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

/**
 * TCP写数据
 *
 * @author hejincheng
 * @date 2022/1/8 15:36
 * @version 1.0
 **/
public class TcpOutPutImp extends KcpOutPutImp {

    @Override
    protected void writeAndFlush(ByteBuf data, User user) {
        user.getChannel().writeAndFlush(data);
    }
}
