package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.log.KtucpLog;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/16 14:56
 **/
public class KtucpOutPutImp implements KtucpOutput {

    protected static final Logger logger = KtucpLog.logger;

    @Override
    public void out(ByteBuf data, IKcp kcp) {
        // 统计
        Snmp.snmp.addOutPackets(1);
        Snmp.snmp.addOutBytes(data.writerIndex());
        ((Kcp) kcp).snmp.addOutPackets(1);
        ((Kcp) kcp).snmp.addOutBytes(data.writerIndex());

        // 回写数据
        User user = (User) kcp.getUser();

        // 使用网络id
        int finalUseNetId = user.getCurrentNetId();

        int forceUseNetId = user.getForceUseNetId();
        if (forceUseNetId != INet.NO_USE_FORCE_NET_ID && KtucpGlobalNetManager.containsNet(forceUseNetId)) {
            // 强制使用网络，并且这个网络存在
            finalUseNetId = forceUseNetId;
        }

        INet net = KtucpGlobalNetManager.getNet(finalUseNetId);
        if (net == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("KtucpOutput writeAndFlush useNet[%d] currentNet[%d] forceUseNet[%d] error : net null", finalUseNetId, user.getCurrentNetId(), forceUseNetId));
            }
            return;
        }
        // 调用网络层真正写数据
        net.send(data, user);
    }
}
