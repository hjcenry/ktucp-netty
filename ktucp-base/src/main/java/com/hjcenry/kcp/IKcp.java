package com.hjcenry.kcp;

import com.hjcenry.fec.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.List;

/**
 * Created by JinMiao
 * 2020/7/20.
 */
public interface IKcp {

    /**
     * 释放
     */
    void release();

    /**
     * 合并接收buf
     *
     * @return
     */
    ByteBuf mergeRecv();

    /**
     * 1，判断是否有完整的包，如果有就抛给下一层
     * 2，整理消息接收队列，判断下一个包是否已经收到 收到放入rcvQueue
     * 3，判断接收窗口剩余是否改变，如果改变记录需要通知
     *
     * @param bufList
     * @return
     */
    int recv(List<ByteBuf> bufList);

    /**
     * check the size of next message in the recv queue
     * 检查接收队列里面是否有完整的一个包，如果有返回该包的字节长度
     *
     * @return -1 没有完整包， >0 一个完整包所含字节
     */
    int peekSize();

    /**
     * 判断一条消息是否完整收全了
     *
     * @return
     */
    boolean canRecv();

    /**
     * 发送
     *
     * @param buf
     * @return
     */
    int send(ByteBuf buf);

    /**
     * kcp输入处理
     *
     * @param data
     * @param regular
     * @param current
     * @return
     */
    int input(ByteBuf data, boolean regular, long current);

    /**
     * 当前时间毫秒
     *
     * @param now
     * @return
     */
    long currentMs(long now);

    /**
     * ikcp_flush
     *
     * @param ackOnly
     * @param current
     * @return
     */
    long flush(boolean ackOnly, long current);

    /**
     * update getState (call it repeatedly, every 10ms-100ms), or you can ask
     * ikcp_check when to call it again (without ikcp_input/_send calling).
     * 'current' - current timestamp in millisec.
     *
     * @param current
     */
    void update(long current);

    /**
     * Determine when should you invoke ikcp_update:
     * returns when you should invoke ikcp_update in millisec, if there
     * is no ikcp_input/_send calling. you can call ikcp_update in that
     * time, instead of call update repeatly.
     * Important to reduce unnacessary ikcp_update invoking. use it to
     * schedule ikcp_update (eg. implementing an epoll-like mechanism,
     * or optimize ikcp_update when handling massive kcp connections)
     *
     * @param current
     * @return
     */
    long check(long current);

    /**
     * 检查是否可以flush
     *
     * @return
     */
    boolean checkFlush();

    /**
     * 设置mtu
     *
     * @param mtu
     * @return
     */
    int setMtu(int mtu);

    /**
     * 获取间隔时间
     *
     * @return
     */
    int getInterval();

    /**
     * 配置
     *
     * @param nodelay
     * @param interval
     * @param resend
     * @param nc
     * @return
     */
    int nodelay(boolean nodelay, int interval, int resend, boolean nc);

    /**
     * 等待发送的数据
     *
     * @return
     */
    int waitSnd();

    /**
     * 获取conv
     *
     * @return
     */
    int getConv();

    /**
     * 设置conv
     *
     * @param conv
     */
    void setConv(int conv);

    /**
     * 获取User对象
     *
     * @return
     */
    Object getUser();

    /**
     * 设置User对象
     *
     * @param user
     */
    void setUser(Object user);

    /**
     * 获取状态
     *
     * @return
     */
    int getState();

    /**
     * 设置状态
     *
     * @param state
     */
    void setState(int state);

    /**
     * 是否是立即发送
     *
     * @return
     */
    boolean isNodelay();

    /**
     * 设置立即发送
     *
     * @param nodelay
     */
    void setNodelay(boolean nodelay);

    /**
     * 设置快速重发次数
     *
     * @param fastresend
     */
    void setFastresend(int fastresend);

    /**
     * 设置最小rto
     *
     * @param rxMinrto
     */
    void setRxMinrto(int rxMinrto);

    /**
     * 设置接受窗口大小
     *
     * @param rcvWnd
     */
    void setRcvWnd(int rcvWnd);

    /**
     * 设置ack大小
     *
     * @param ackMaskSize
     */
    void setAckMaskSize(int ackMaskSize);

    /**
     * @param reserved
     */
    void setReserved(int reserved);

    /**
     * 发送窗口大小
     *
     * @return
     */
    int getSndWnd();

    /**
     * 设置发送窗口大小
     *
     * @param sndWnd
     */
    void setSndWnd(int sndWnd);

    /**
     * 是否是流模式
     *
     * @return
     */
    boolean isStream();

    /**
     * 设置流模式
     *
     * @param stream
     */
    void setStream(boolean stream);

    /**
     * 设置buf分配器
     *
     * @param byteBufAllocator
     */
    void setByteBufAllocator(ByteBufAllocator byteBufAllocator);

    /**
     * 获取输出接口
     *
     * @return
     */
    KtucpOutput getOutput();

    /**
     * 设置输出接口
     *
     * @param output
     */
    void setOutput(KtucpOutput output);

    /**
     * 设置立即回复ack
     *
     * @param ackNoDelay
     */
    void setAckNoDelay(boolean ackNoDelay);
}
