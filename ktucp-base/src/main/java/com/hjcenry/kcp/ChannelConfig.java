package com.hjcenry.kcp;

import com.hjcenry.fec.FecAdapt;
import com.hjcenry.threadpool.IMessageExecutorPool;
import com.hjcenry.threadpool.netty.NettyMessageExecutorPool;
import com.hjcenry.time.IKtucpTimeService;
import com.hjcenry.time.SystemTimeServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JinMiao
 * 2018/9/20.
 */
public class ChannelConfig {
    public static final int CRC_32_SIZE = 4;

    private int conv;
    private boolean nodelay;
    private int interval = Kcp.IKCP_INTERVAL;
    private int fastResend;
    private boolean nocWnd;
    private int sndWnd = Kcp.IKCP_WND_SND;
    private int rcvWnd = Kcp.IKCP_WND_RCV;
    private int mtu = Kcp.IKCP_MTU_DEF;
    /**
     * 超时时间 超过一段时间没收到消息断开连接
     */
    private long timeoutMillis;
    //TODO 可能有bug还未测试
    /**
     * 流模式
     */
    private boolean stream;

    // 下面为新增参数
    /**
     * fec适配
     */
    private FecAdapt fecAdapt;
    /**
     * 收到包立刻回传ack包
     */
    private boolean ackNoDelay = false;
    /**
     * 发送包立即调用flush 延迟低一些  cpu增加  如果interval值很小 建议关闭该参数
     */
    private boolean fastFlush = true;
    /**
     * crc32校验
     */
    private boolean crc32Check = false;
    /**
     * 接收窗口大小(字节 -1不限制)
     */
    private int readBufferSize = -1;
    /**
     * 发送窗口大小(字节 -1不限制)
     */
    private int writeBufferSize = -1;
    /**
     * 增加ack包回复成功率 填 /8/16/32
     */
    private int ackMaskSize = 0;
    /**
     * 使用conv确定一个channel 还是使用 socketAddress确定一个channel
     **/
    private boolean useConvChannel = true;
    /**
     * 处理kcp消息接收和发送的线程池
     **/
    private IMessageExecutorPool messageExecutorPool = new NettyMessageExecutorPool(Runtime.getRuntime().availableProcessors());
    /**
     * 网络切换最小检测时间长度
     */
    private long netChangeMinPeriod = 30 * 1000;
    /**
     * 网络切换最大次数，超过这个次数，则进行处理
     */
    private int netChangeMaxCount = 0;
    /**
     * 网络配置
     * <b>有多少配置，就会启动多少网络服务</b>
     */
    private final List<INetChannelConfig> netChannelConfigList = new ArrayList<>();
    /**
     * 时间服务，默认取系统时间
     */
    private IKtucpTimeService timeService = new SystemTimeServiceImpl();
    /**
     * kcp闲置超时是否关闭连接
     * 默认true，不关闭则需要用户主动调用close方法，否则KCP对象会一致持有
     */
    private boolean kcpIdleTimeoutClose = true;
    /**
     * 启动完成回调
     */
    private IKtucpServerStartUpCallback serverStartUpCallback;
    private IKtucpClientStartUpCallback clientStartUpCallback;

    public void nodelay(boolean nodelay, int interval, int resend, boolean nc) {
        this.nodelay = nodelay;
        this.interval = interval;
        this.fastResend = resend;
        this.nocWnd = nc;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public IMessageExecutorPool getMessageExecutorPool() {
        return messageExecutorPool;
    }

    public void setMessageExecutorPool(IMessageExecutorPool messageExecutorPool) {
        if (this.messageExecutorPool != null) {
            this.messageExecutorPool.stop();
        }
        this.messageExecutorPool = messageExecutorPool;
    }

    public boolean isNodelay() {
        return nodelay;
    }

    public int getConv() {
        return conv;
    }

    public void setConv(int conv) {
        this.conv = conv;
    }

    public int getInterval() {
        return interval;
    }

    public int getFastResend() {
        return fastResend;
    }

    public boolean isNocWnd() {
        return nocWnd;
    }

    public int getSndWnd() {
        return sndWnd;
    }

    public void setSndWnd(int sndWnd) {
        this.sndWnd = sndWnd;
    }

    public int getRcvWnd() {
        return rcvWnd;
    }

    public void setRcvWnd(int rcvWnd) {
        this.rcvWnd = rcvWnd;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public FecAdapt getFecAdapt() {
        return fecAdapt;
    }

    public void setFecAdapt(FecAdapt fecAdapt) {
        this.fecAdapt = fecAdapt;
    }

    public boolean isAckNoDelay() {
        return ackNoDelay;
    }

    public void setAckNoDelay(boolean ackNoDelay) {
        this.ackNoDelay = ackNoDelay;
    }

    public boolean isFastFlush() {
        return fastFlush;
    }

    public void setFastFlush(boolean fastFlush) {
        this.fastFlush = fastFlush;
    }

    public boolean isCrc32Check() {
        return crc32Check;
    }

    public int getAckMaskSize() {
        return ackMaskSize;
    }

    public void setAckMaskSize(int ackMaskSize) {
        this.ackMaskSize = ackMaskSize;
    }

    public void setCrc32Check(boolean crc32Check) {
        this.crc32Check = crc32Check;
    }

    public boolean isUseConvChannel() {
        return useConvChannel;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public void setUseConvChannel(boolean useConvChannel) {
        this.useConvChannel = useConvChannel;
    }

    public List<INetChannelConfig> getNetChannelConfigList() {
        return netChannelConfigList;
    }

    public void addNetChannelConfig(INetChannelConfig netChannelConfig) {
        this.netChannelConfigList.add(netChannelConfig);
    }

    public int getNetNum() {
        return netChannelConfigList.size();
    }

    public long getNetChangeMinPeriod() {
        return netChangeMinPeriod;
    }

    public void setNetChangeMinPeriod(long netChangeMinPeriod) {
        this.netChangeMinPeriod = netChangeMinPeriod;
    }

    public int getNetChangeMaxCount() {
        return netChangeMaxCount;
    }

    public void setNetChangeMaxCount(int netChangeMaxCount) {
        this.netChangeMaxCount = netChangeMaxCount;
    }

    public IKtucpTimeService getTimeService() {
        return timeService;
    }

    public void setTimeService(IKtucpTimeService timeService) {
        this.timeService = timeService;
    }

    public boolean isKcpIdleTimeoutClose() {
        return kcpIdleTimeoutClose;
    }

    public void setKcpIdleTimeoutClose(boolean kcpIdleTimeoutClose) {
        this.kcpIdleTimeoutClose = kcpIdleTimeoutClose;
    }

    public IKtucpServerStartUpCallback getServerStartUpCallback() {
        return serverStartUpCallback;
    }

    public void setServerStartUpCallback(IKtucpServerStartUpCallback serverStartUpCallback) {
        this.serverStartUpCallback = serverStartUpCallback;
    }

    public IKtucpClientStartUpCallback getClientStartUpCallback() {
        return clientStartUpCallback;
    }

    public void setClientStartUpCallback(IKtucpClientStartUpCallback clientStartUpCallback) {
        this.clientStartUpCallback = clientStartUpCallback;
    }
}
