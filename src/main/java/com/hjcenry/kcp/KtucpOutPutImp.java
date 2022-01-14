package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.log.KtucpLog;
import com.hjcenry.net.INet;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

/**
 * Created by JinMiao
 * 2018/9/21.
 */
public class KtucpOutPutImp implements KtucpOutput {

    protected static final Logger logger = KtucpLog.logger;

    @Override
    public void out(ByteBuf data, IKcp kcp) {
        // 统计
        Snmp.snmp.OutPkts.increment();
        Snmp.snmp.OutBytes.add(data.writerIndex());
        // 回写数据
        User user = (User) kcp.getUser();

        INet net = KtucpNetManager.getNet(user.getCurrentNetId());
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
