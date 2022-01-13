package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.log.KcpLog;
import com.hjcenry.net.INet;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JinMiao
 * 2018/9/21.
 */
public class KcpOutPutImp implements KcpOutput {

    protected static final Logger logger = KcpLog.logger;

    @Override
    public void out(ByteBuf data, IKcp kcp) {
        // 统计
        Snmp.snmp.OutPkts.increment();
        Snmp.snmp.OutBytes.add(data.writerIndex());
        // 回写数据
        User user = (User) kcp.getUser();

        INet net = KcpNetManager.getNet(user.getCurrentNetId());
        if (net == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("KcpOutput writeAndFlush currentNet[%d] error : net null", user.getCurrentNetId()));
            }
            return;
        }
        // 调用网络层真正写数据
        net.send(data, user);
    }
}
