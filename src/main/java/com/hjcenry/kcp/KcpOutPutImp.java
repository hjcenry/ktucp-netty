package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by JinMiao
 * 2018/9/21.
 */
public abstract class KcpOutPutImp implements KcpOutput {

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
    protected abstract void writeAndFlush(ByteBuf data, User user);
}
