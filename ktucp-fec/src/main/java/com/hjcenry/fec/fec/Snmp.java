package com.hjcenry.fec.fec;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * 网络Snmp数据统计
 * <p>
 * 可通过启动参数-Dktucp.snmp=false关闭打印，默认开启打印
 * </p>
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/16 11:07
 **/
public class Snmp {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(Snmp.class);

    /**
     * 是否开启统计
     */
    public static final boolean OPEN_SNMP;

    static {
        boolean configOpenSnmp;
        String configKey = "ktucp.snmp";
        String configVal = System.getProperty(configKey);
        if (configVal != null) {
            try {
                configOpenSnmp = Boolean.parseBoolean(configVal);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn(String.format("-D%s=%s is invalid, please check", configKey, configVal), e);
                }
                configOpenSnmp = true;
            }
        } else {
            // 默认开启
            configOpenSnmp = true;
        }
        OPEN_SNMP = configOpenSnmp;
    }

    /**
     * bytes sent from upper level
     */
    private LongAdder bytesSent = new LongAdder();

    public void addBytesSent(long num) {
        if (OPEN_SNMP) {
            bytesSent.add(num);
        }
    }

    /**
     * bytes received to upper level
     */
    private LongAdder bytesReceived = new LongAdder();

    public void addBytesReceived(long num) {
        if (OPEN_SNMP) {
            bytesReceived.add(num);
        }
    }

    /**
     * max number of connections ever reached
     */
    private LongAdder maxConn = new LongAdder();
    /**
     * accumulated active open connections
     */
    private LongAdder activeOpens = new LongAdder();
    /**
     * accumulated passive open connections
     */
    private LongAdder passiveOpens = new LongAdder();
    /**
     * current number of established connections
     */
    private LongAdder currEstablished = new LongAdder();
    /**
     * Net read errors reported from net.PacketConn
     */
    private LongAdder inErrs = new LongAdder();

    public void addInErrs(int num) {
        if (OPEN_SNMP) {
            inErrs.add(num);
        }
    }

    /**
     * checksum errors from CRC32
     */
    private LongAdder inCheckSumErrors = new LongAdder();

    public void addInCheckSumErrors(int num) {
        if (OPEN_SNMP) {
            inCheckSumErrors.add(num);
        }
    }

    /**
     * packet input errors reported from KCP
     */
    private LongAdder kcpInErrors = new LongAdder();

    public void addKcpInErrors(int num) {
        if (OPEN_SNMP) {
            kcpInErrors.add(num);
        }
    }

    /**
     * incoming packets count
     */
    private LongAdder inPackets = new LongAdder();

    public void addInPackets(int num) {
        if (OPEN_SNMP) {
            inPackets.add(num);
        }
    }

    /**
     * outgoing packets count
     */
    private LongAdder outPackets = new LongAdder();

    public void addOutPackets(int num) {
        if (OPEN_SNMP) {
            outPackets.add(num);
        }
    }

    /**
     * incoming KCP segments
     */
    private LongAdder inSegments = new LongAdder();

    public void addInSegments(int num) {
        if (OPEN_SNMP) {
            inSegments.add(num);
        }
    }

    /**
     * outgoing KCP segments
     */
    private LongAdder outSegments = new LongAdder();

    public void addOutSegments(int num) {
        if (OPEN_SNMP) {
            outSegments.add(num);
        }
    }

    /**
     * Net bytes received
     */
    private LongAdder inBytes = new LongAdder();

    public void addInBytes(int num) {
        if (OPEN_SNMP) {
            inBytes.add(num);
        }
    }

    /**
     * Net bytes sent
     */
    private LongAdder outBytes = new LongAdder();

    public void addOutBytes(int num) {
        if (OPEN_SNMP) {
            outBytes.add(num);
        }
    }

    /**
     * accumulate retransmited segments
     */
    private LongAdder retransmitSegments = new LongAdder();

    public void addRetransmitSegments(int num) {
        if (OPEN_SNMP) {
            retransmitSegments.add(num);
        }
    }

    /**
     * accumulate fast retransmitted segments
     */
    private LongAdder fastRetransmitSegments = new LongAdder();

    public void addFastRetransmitSegments(int num) {
        if (OPEN_SNMP) {
            fastRetransmitSegments.add(num);
        }
    }

    /**
     * accumulate early retransmitted segments
     */
    private LongAdder earlyRetransmitSegments = new LongAdder();

    public void addEarlyRetransmitSegments(int num) {
        if (OPEN_SNMP) {
            earlyRetransmitSegments.add(num);
        }
    }

    /**
     * number of segs infered as lost
     */
    private LongAdder lostSegments = new LongAdder();

    public void addLostSegments(int num) {
        if (OPEN_SNMP) {
            lostSegments.add(num);
        }
    }

    /**
     * number of segs duplicated
     */
    private LongAdder repeatSegments = new LongAdder();

    public void addRepeatSegments(int num) {
        if (OPEN_SNMP) {
            repeatSegments.add(num);
        }
    }

    /**
     * correct packets recovered from FEC
     */
    private LongAdder fecRecovered = new LongAdder();

    public void addFecRecovered(int num) {
        if (OPEN_SNMP) {
            fecRecovered.add(num);
        }
    }

    /**
     * incorrect packets recovered from FEC
     */
    private LongAdder fecErrs = new LongAdder();

    public void addFecErrs(int num) {
        if (OPEN_SNMP) {
            fecErrs.add(num);
        }
    }

    /**
     * 收到的 Data数量
     */
    private LongAdder fecDataShards = new LongAdder();

    public void addFecDataShards(int num) {
        if (OPEN_SNMP) {
            fecDataShards.add(num);
        }
    }

    /**
     * 收到的 Parity数量
     */
    private LongAdder fecParityShards = new LongAdder();

    public void addFecParityShards(int num) {
        if (OPEN_SNMP) {
            fecParityShards.add(num);
        }
    }

    /**
     * number of data shards that's not enough for recovery
     */
    private LongAdder fecShortShards = new LongAdder();

    public void addFecShortShards(int num) {
        if (OPEN_SNMP) {
            fecShortShards.add(num);
        }
    }

    /**
     * number of data shards that's not enough for recovery
     */
    private LongAdder fecRepeatDataShards = new LongAdder();

    public void addFecRepeatDataShards(int num) {
        if (OPEN_SNMP) {
            fecRepeatDataShards.add(num);
        }
    }

    public LongAdder getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(LongAdder bytesSent) {
        this.bytesSent = bytesSent;
    }

    public LongAdder getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(LongAdder bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public LongAdder getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(LongAdder maxConn) {
        this.maxConn = maxConn;
    }

    public LongAdder getActiveOpens() {
        return activeOpens;
    }

    public void setActiveOpens(LongAdder activeOpens) {
        this.activeOpens = activeOpens;
    }

    public LongAdder getPassiveOpens() {
        return passiveOpens;
    }

    public void setPassiveOpens(LongAdder passiveOpens) {
        this.passiveOpens = passiveOpens;
    }

    public LongAdder getCurrEstablished() {
        return currEstablished;
    }

    public void setCurrEstablished(LongAdder currEstablished) {
        this.currEstablished = currEstablished;
    }

    public LongAdder getInErrs() {
        return inErrs;
    }

    public void setInErrs(LongAdder inErrs) {
        this.inErrs = inErrs;
    }

    public LongAdder getInCheckSumErrors() {
        return inCheckSumErrors;
    }

    public void setInCheckSumErrors(LongAdder inCheckSumErrors) {
        this.inCheckSumErrors = inCheckSumErrors;
    }

    public LongAdder getKcpInErrors() {
        return kcpInErrors;
    }

    public void setKcpInErrors(LongAdder kcpInErrors) {
        this.kcpInErrors = kcpInErrors;
    }

    public LongAdder getInPackets() {
        return inPackets;
    }

    public void setInPackets(LongAdder inPackets) {
        this.inPackets = inPackets;
    }

    public LongAdder getOutPackets() {
        return outPackets;
    }

    public void setOutPackets(LongAdder outPackets) {
        this.outPackets = outPackets;
    }

    public LongAdder getInSegments() {
        return inSegments;
    }

    public void setInSegments(LongAdder inSegments) {
        this.inSegments = inSegments;
    }

    public LongAdder getOutSegments() {
        return outSegments;
    }

    public void setOutSegments(LongAdder outSegments) {
        this.outSegments = outSegments;
    }

    public LongAdder getInBytes() {
        return inBytes;
    }

    public void setInBytes(LongAdder inBytes) {
        this.inBytes = inBytes;
    }

    public LongAdder getOutBytes() {
        return outBytes;
    }

    public void setOutBytes(LongAdder outBytes) {
        this.outBytes = outBytes;
    }

    public LongAdder getRetransmitSegments() {
        return retransmitSegments;
    }

    public void setRetransmitSegments(LongAdder retransmitSegments) {
        this.retransmitSegments = retransmitSegments;
    }

    public LongAdder getFastRetransmitSegments() {
        return fastRetransmitSegments;
    }

    public void setFastRetransmitSegments(LongAdder fastRetransmitSegments) {
        this.fastRetransmitSegments = fastRetransmitSegments;
    }

    public LongAdder getEarlyRetransmitSegments() {
        return earlyRetransmitSegments;
    }

    public void setEarlyRetransmitSegments(LongAdder earlyRetransmitSegments) {
        this.earlyRetransmitSegments = earlyRetransmitSegments;
    }

    public LongAdder getLostSegments() {
        return lostSegments;
    }

    public void setLostSegments(LongAdder lostSegments) {
        this.lostSegments = lostSegments;
    }

    public LongAdder getRepeatSegments() {
        return repeatSegments;
    }

    public void setRepeatSegments(LongAdder repeatSegments) {
        this.repeatSegments = repeatSegments;
    }

    public LongAdder getFecRecovered() {
        return fecRecovered;
    }

    public void setFecRecovered(LongAdder fecRecovered) {
        this.fecRecovered = fecRecovered;
    }

    public LongAdder getFecErrs() {
        return fecErrs;
    }

    public void setFecErrs(LongAdder fecErrs) {
        this.fecErrs = fecErrs;
    }

    public LongAdder getFecDataShards() {
        return fecDataShards;
    }

    public void setFecDataShards(LongAdder fecDataShards) {
        this.fecDataShards = fecDataShards;
    }

    public LongAdder getFecParityShards() {
        return fecParityShards;
    }

    public void setFecParityShards(LongAdder fecParityShards) {
        this.fecParityShards = fecParityShards;
    }

    public LongAdder getFecShortShards() {
        return fecShortShards;
    }

    public void setFecShortShards(LongAdder fecShortShards) {
        this.fecShortShards = fecShortShards;
    }

    public LongAdder getFecRepeatDataShards() {
        return fecRepeatDataShards;
    }

    public void setFecRepeatDataShards(LongAdder fecRepeatDataShards) {
        this.fecRepeatDataShards = fecRepeatDataShards;
    }

    /**
     * 全局统计数据
     */
    public static volatile Snmp snmp = new Snmp();

    @Override
    public String toString() {
        return "Snmp{" +
                "发送字节=" + bytesSent +
                ", 收到字节=" + bytesReceived +
//                ", MaxConn=" + maxConn +
//                ", ActiveOpens=" + activeOpens +
//                ", PassiveOpens=" + passiveOpens +
//                ", CurrEstab=" + currEstablished +
//                ", InErrs=" + inErrs +
//                ", InCsumErrors=" + inCheckSumErrors +
//                ", KCPInErrors=" + kcpInErrors +
                ", 收到包=" + inPackets +
                ", 发送包=" + outPackets +
                ", 收到分片=" + inSegments +
                ", 发送分片=" + outSegments +
                ", 收到字节=" + inBytes +
                ", 发送字节=" + outBytes +
                ", 总共重发数=" + retransmitSegments +
                ", 快速重发数=" + fastRetransmitSegments +
                ", 空闲快速重发数=" + earlyRetransmitSegments +
                ", 超时重发数=" + lostSegments +
                ", 收到重复包数量=" + repeatSegments +
                ", fec恢复数=" + fecRecovered +
                ", fec恢复错误数=" + fecErrs +
                ", 收到fecData数=" + fecDataShards +
                ", 收到fecParity数=" + fecParityShards +
                ", fec缓存冗余淘汰data包数=" + fecShortShards +
                ", fec收到重复的数据包=" + fecRepeatDataShards +
                '}';
    }

}
