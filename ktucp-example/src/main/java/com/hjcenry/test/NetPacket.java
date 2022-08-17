package com.hjcenry.test;

import com.hjcenry.util.DateUtil;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName KcpPacket
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 17:05
 * @Version 1.0
 **/
@Data
public class NetPacket {

    private int id;

    private long createTime;

    private long serverReceiveTime;

    private long returnTime;

    private long receivePacketBytes;
    private long sendPacketBytes;

    public NetPacket(int id, long createTime) {
        this.id = id;
        this.createTime = createTime;
        this.receivePacketBytes = 0;
        this.sendPacketBytes = 0;
    }

    public void addReceivePackets(long bytes) {
        this.receivePacketBytes += bytes;
    }

    public void addSendPacketBytes(long bytes) {
        this.sendPacketBytes += bytes;
    }

    /**
     * 总延时
     *
     * @return
     */
    public long totalDelay() {
        return returnTime - createTime;
    }

    public boolean isValid() {
        if (serverReceiveTime <= 0) {
            return false;
        }
        if (createTime <= 0) {
            return false;
        }
        if (returnTime <= 0) {
            return false;
        }
        return true;
    }

    public boolean isReplied() {
        return returnTime > 0;
    }

    public String getCreateTimeFormat() {
        if (this.createTime == 0) {
            return "";
        }
        return DateUtil.getTime(new Date(this.createTime), "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public String getServerReceiveTimeFormat() {
        if (this.serverReceiveTime == 0) {
            return "";
        }
        return DateUtil.getTime(new Date(this.serverReceiveTime), "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public String getReturnTimeFormat() {
        if (this.returnTime == 0) {
            return "";
        }
        return DateUtil.getTime(new Date(this.returnTime), "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public double getReceivePacketMB() {
        return receivePacketBytes / 1024d / 1024d;
    }

    public double getSendPacketMB() {
        return sendPacketBytes / 1024d / 1024d;
    }

    public long getReceivePacketBytes() {
        return receivePacketBytes;
    }

    public long getSendPacketBytes() {
        return sendPacketBytes;
    }

    @Override
    public String toString() {
        return "NetPacket{" +
                "id=" + id +
                ", 客户端发送时间=" + getCreateTimeFormat() +
                ", 服务端接收时间=" + getServerReceiveTimeFormat() +
                ", 客户端接收时间=" + getReturnTimeFormat() +
                ", 单程延时=" + totalDelay() / 2f + "ms" +
                ", 总延时=" + totalDelay() + "ms" +
                ", 接收包=" + getReceivePacketBytes() + "kb" +
                ", 发送包=" + getSendPacketBytes() + "kb" +
                '}';
    }
}
