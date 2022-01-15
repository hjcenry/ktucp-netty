package com.hjcenry.kcp;

import com.hjcenry.codec.decode.IMessageDecoder;
import com.hjcenry.codec.encode.IMessageEncoder;
import com.hjcenry.fec.FecAdapt;
import com.hjcenry.fec.IFecDecode;
import com.hjcenry.fec.IFecEncode;
import com.hjcenry.fec.fec.Fec;
import com.hjcenry.fec.fec.FecPacket;
import com.hjcenry.fec.fec.Snmp;
import com.hjcenry.kcp.listener.KtucpListener;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.time.IKtucpTimeService;
import com.hjcenry.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jctools.queues.MpscLinkedQueue;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Uktucp extends DefaultAttributeMap {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(Uktucp.class);

    private final IKcp kcp;

    private boolean fastFlush = true;

    private long tsUpdate = -1;

    private boolean active;

    private IFecEncode fecEncode = null;
    private IFecDecode fecDecode = null;

    /**
     * 写消息队列：按Object写
     */
    private final Queue<Object> writeObjectQueue;
    /**
     * 对消息队列：按Buffer读
     */
    private final Queue<ByteBuf> readBufferQueue;

    private final IMessageExecutor messageExecutor;

    private final KtucpListener ktucpListener;

    private final IChannelManager channelManager;

    private final AtomicBoolean writeProcessing = new AtomicBoolean(false);

    private final AtomicBoolean readProcessing = new AtomicBoolean(false);

    private final AtomicInteger readBufferIncr = new AtomicInteger(-1);

    private final AtomicInteger writeBufferIncr = new AtomicInteger(-1);

    private final WriteTask writeTask;

    private final ReadTask readTask;

    /**
     * 超时时间可修改
     */
    private long timeoutMillis;

    private boolean controlReadBufferSize = false;

    private boolean controlWriteBufferSize = false;
    /**
     * 上次收到消息时间
     **/
    private long lastReceiveTime;
    /**
     * 网络切换最小检测时间长度
     */
    private long netChangeMinPeriod = 30 * 1000;
    /**
     * 网络切换最大次数，超过这个次数，则进行处理
     */
    private int netChangeMaxCount = 0;
    /**
     * 时间服务
     */
    private IKtucpTimeService kcpTimeService;
    /**
     * 是否处理过超时
     */
    private boolean handledTimeout;

    /**
     * Creates a new instance.
     *
     * @param output output for kcp
     */
    public Uktucp(KtucpOutput output,
                  KtucpListener ktucpListener,
                  IMessageExecutor messageExecutor,
                  ChannelConfig channelConfig,
                  IChannelManager channelManager,
                  IMessageEncoder messageEncoder,
                  IMessageDecoder messageDecoder) {
        this.kcpTimeService = channelConfig.getTimeService();
        long now = this.kcpTimeService.now();

        this.timeoutMillis = channelConfig.getTimeoutMillis();
        this.lastReceiveTime = now;

        // 网络切换检测时间
        long configNetChangeMinPeriod = channelConfig.getNetChangeMinPeriod();
        if (configNetChangeMinPeriod > 0) {
            this.netChangeMinPeriod = configNetChangeMinPeriod;
        }
        this.netChangeMinPeriod = channelConfig.getNetChangeMinPeriod();
        // 网络最大切换次数
        int configNetChangeMaxCount = channelConfig.getNetChangeMaxCount();
        if (configNetChangeMaxCount > 0) {
            this.netChangeMaxCount = configNetChangeMaxCount;
        }

        this.kcp = new Kcp(channelConfig.getConv(), now, output);
        this.active = true;
        this.ktucpListener = ktucpListener;
        this.messageExecutor = messageExecutor;
        this.channelManager = channelManager;

        // 写任务
        this.writeTask = new WriteTask(this, messageEncoder);
        // 读任务
        this.readTask = new ReadTask(this, messageDecoder);

        this.writeObjectQueue = new MpscLinkedQueue<>();
        this.readBufferQueue = new MpscLinkedQueue<>();

        if (channelConfig.getReadBufferSize() != -1) {
            this.controlReadBufferSize = true;
            this.readBufferIncr.set(channelConfig.getReadBufferSize() / channelConfig.getMtu());
        }

        if (channelConfig.getWriteBufferSize() != -1) {
            this.controlWriteBufferSize = true;
            this.writeBufferIncr.set(channelConfig.getWriteBufferSize() / channelConfig.getMtu());
        }

        int headerSize = 0;
        FecAdapt fecAdapt = channelConfig.getFecAdapt();
        if (channelConfig.isCrc32Check()) {
            headerSize += ChannelConfig.crc32Size;
        }

        //init fec
        if (fecAdapt != null) {
            KtucpOutput ktucpOutput = kcp.getOutput();
            fecEncode = fecAdapt.fecEncode(headerSize, channelConfig.getMtu());
            fecDecode = fecAdapt.fecDecode(channelConfig.getMtu());
            ktucpOutput = new FecOutPut(ktucpOutput, fecEncode);
            kcp.setOutput(ktucpOutput);
            headerSize += Fec.fecHeaderSizePlus2;
        }

        kcp.setReserved(headerSize);
        initKcpConfig(channelConfig);
    }

    public void setClientMode() {
        user().setClient(true);
    }

    public void setServerMode() {
        user().setClient(false);
    }

    /**
     * 网络切换计数
     */
    private AtomicInteger netChangeCounter = new AtomicInteger(0);
    /**
     * 网络切换计数过期时间
     */
    private long netChangeExpireTime = 0L;

    /**
     * 修改网络
     * <p>直接修改，不提供网络切换踢下线策略，需要切换检测调用{@link Uktucp#changeCurrentNetId(int)}</p>
     *
     * @param netId 网络id
     */
    public void setCurrentNetId(int netId) {
        // 设置当前网络id
        User user = this.user();
        user.setCurrentNetId(netId);
    }

    /**
     * 切换网络
     * <p>客户端可自定切换策略</p>
     * <p>提供网络切换踢下线策略</p>
     *
     * @param netId 网络id
     */
    public void changeCurrentNetId(int netId) {
        User user = this.user();
        int oldNetId = user.getCurrentNetId();
        // 设置当前网络id
        this.setCurrentNetId(netId);

        if (netChangeMaxCount <= 0 || netChangeMinPeriod <= 0) {
            // 未配置开启网络切换检测
            return;
        }

        // 网络切换
        if (oldNetId != netId) {
            // 网络切换计数
            long now = this.kcpTimeService.now();
            if (now >= this.netChangeExpireTime) {
                // 计数超时，归零，计数1
                netChangeCounter.set(1);
                this.netChangeExpireTime = now + netChangeMinPeriod;
                return;
            }
            // 切换计数
            int counter = netChangeCounter.incrementAndGet();
            if (counter > netChangeMaxCount) {
                // 切换计数超了最大值，关闭所有连接
                this.closeChannel();
            }
        }
    }

    private void initKcpConfig(ChannelConfig channelConfig) {
        kcp.nodelay(channelConfig.isNodelay(), channelConfig.getInterval(), channelConfig.getFastResend(), channelConfig.isNocWnd());
        kcp.setSndWnd(channelConfig.getSndWnd());
        kcp.setRcvWnd(channelConfig.getRcvWnd());
        kcp.setMtu(channelConfig.getMtu());
        kcp.setStream(channelConfig.isStream());
        kcp.setAckNoDelay(channelConfig.isAckNoDelay());
        kcp.setAckMaskSize(channelConfig.getAckMaskSize());
        this.fastFlush = channelConfig.isFastFlush();
    }

    /**
     * Receives ByteBufs.
     *
     * @param bufList received ByteBuf will be add to the list
     */
    protected void receive(List<ByteBuf> bufList) {
        kcp.recv(bufList);
    }

    protected ByteBuf mergeReceive() {
        return kcp.mergeRecv();
    }

    protected void input(ByteBuf data, long current) throws IOException {
        Snmp.snmp.InPkts.increment();
        Snmp.snmp.InBytes.add(data.readableBytes());

        if (fecDecode != null) {
            FecPacket fecPacket = FecPacket.newFecPacket(data);
            if (fecPacket.getFlag() == Fec.typeData) {
                data.skipBytes(2);
                input(data, true, current);
            }
            if (fecPacket.getFlag() == Fec.typeData || fecPacket.getFlag() == Fec.typeParity) {
                List<ByteBuf> byteBufs = fecDecode.decode(fecPacket);
                if (byteBufs != null) {
                    ByteBuf byteBuf;
                    for (ByteBuf buf : byteBufs) {
                        byteBuf = buf;
                        input(byteBuf, false, current);
                        byteBuf.release();
                    }
                }
            }
        } else {
            input(data, true, current);
        }
    }

    private void input(ByteBuf data, boolean regular, long current) throws IOException {
        int ret = kcp.input(data, regular, current);
        switch (ret) {
            case -1:
                throw new IOException("No enough bytes of head");
            case -2:
                throw new IOException("No enough bytes of data");
            case -3:
                throw new IOException("Mismatch cmd");
            case -4:
                throw new IOException("Conv inconsistency");
            default:
                break;
        }
    }

    /**
     * Sends a Bytebuf.
     *
     * @param buf
     * @throws IOException
     */
    void send(ByteBuf buf) throws IOException {
        int ret = kcp.send(buf);
        switch (ret) {
            case -2:
                throw new IOException("Too many fragments");
            default:
                break;
        }
    }

    /**
     * Returns {@code true} if there are bytes can be received.
     *
     * @return
     */
    protected boolean canRecv() {
        return kcp.canRecv();
    }

    protected long getLastReceiveTime() {
        return lastReceiveTime;
    }

    protected void setLastReceiveTime(long lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }

    /**
     * Returns {@code true} if the kcp can send more bytes.
     *
     * @param curCanSend last state of canSend
     * @return {@code true} if the kcp can send more bytes
     */
    protected boolean canSend(boolean curCanSend) {
        int max = kcp.getSndWnd() * 2;
        int waitSnd = kcp.waitSnd();
        if (curCanSend) {
            return waitSnd < max;
        } else {
            int threshold = Math.max(1, max / 2);
            return waitSnd < threshold;
        }
    }

    /**
     * Udpates the kcp.
     *
     * @param current current time in milliseconds
     * @return the next time to update
     */
    protected long update(long current) {
        kcp.update(current);
        long nextTsUp = check(current);
        setTsUpdate(nextTsUp);

        return nextTsUp;
    }

    protected long flush(long current) {
        return kcp.flush(false, current);
    }

    /**
     * Determines when should you invoke udpate.
     *
     * @param current current time in milliseconds
     * @return
     * @see Kcp#check(long)
     */
    protected long check(long current) {
        return kcp.check(current);
    }

    /**
     * Returns {@code true} if the kcp need to flush.
     *
     * @return {@code true} if the kcp need to flush
     */
    protected boolean checkFlush() {
        return kcp.checkFlush();
    }

    /**
     * Returns conv of kcp.
     *
     * @return conv of kcp
     */
    public int getConv() {
        return kcp.getConv();
    }

    /**
     * Set the conv of kcp.
     *
     * @param conv the conv of kcp
     */
    public void setConv(int conv) {
        kcp.setConv(conv);
    }

    /**
     * Returns update interval.
     *
     * @return update interval
     */
    protected int getInterval() {
        return kcp.getInterval();
    }

    protected boolean isStream() {
        return kcp.isStream();
    }

    /**
     * Sets the {@link ByteBufAllocator} which is used for the kcp to allocate buffers.
     *
     * @param allocator the allocator is used for the kcp to allocate buffers
     * @return this object
     */
    public Uktucp setByteBufAllocator(ByteBufAllocator allocator) {
        kcp.setByteBufAllocator(allocator);
        return this;
    }

    protected boolean isFastFlush() {
        return fastFlush;
    }

    protected void read(ByteBuf byteBuf) {
        if (controlReadBufferSize) {
            int readBufferSize = readBufferIncr.getAndUpdate(operand -> {
                if (operand == 0) {
                    return operand;
                }
                return --operand;
            });
            if (readBufferSize == 0) {
                //TODO 这里做的不对 应该丢弃队列最早的那个消息包  这样子丢弃有一定的概率会卡死 以后优化
                byteBuf.release();
                log.error("conv {} readBuffer is full", kcp.getConv());
                return;
            }
        }
        this.readBufferQueue.offer(byteBuf);
        notifyReadEvent();
    }

    /**
     * 发送有序可靠消息<br/>
     * 线程安全的<br/>
     * <p>
     * 写ByteBuf时不要调用release，内部会调用，否则可能抛出{@link io.netty.util.IllegalReferenceCountException}异常
     * </p>
     *
     * @param byteBuf 发送后需要手动调用 {@link ByteBuf#release()}
     * @return true发送成功  false缓冲区满了
     */
    public boolean write(Object object) {
        if (controlWriteBufferSize) {
            int bufferSize = writeBufferIncr.getAndUpdate(operand -> {
                if (operand == 0) {
                    return operand;
                }
                return --operand;
            });
            if (bufferSize == 0) {
                //log.error("conv {} address {} writeBuffer is full",kcp.getConv(),((User)kcp.getUser()).getRemoteAddress());
                return false;
            }
        }
        writeObjectQueue.offer(object);
        notifyWriteEvent();
        return true;
    }

    protected AtomicInteger getReadBufferIncr() {
        return readBufferIncr;
    }

    /**
     * 主动关闭连接调用
     */
    public void close() {
        this.messageExecutor.execute(() -> internalClose());
    }

    private void notifyReadEvent() {
        if (readProcessing.compareAndSet(false, true)) {
            this.messageExecutor.execute(this.readTask);
        }
    }

    protected void notifyWriteEvent() {
        if (writeProcessing.compareAndSet(false, true)) {
            this.messageExecutor.execute(this.writeTask);
        }
    }

    protected long getTsUpdate() {
        return tsUpdate;
    }

    protected Queue<ByteBuf> getReadBufferQueue() {
        return readBufferQueue;
    }

    protected Uktucp setTsUpdate(long tsUpdate) {
        this.tsUpdate = tsUpdate;
        return this;
    }

    protected Queue<Object> getWriteObjectQueue() {
        return writeObjectQueue;
    }

    protected KtucpListener getKcpListener() {
        return ktucpListener;
    }

    public boolean isActive() {
        return active;
    }

    void internalClose() {
        if (!active) {
            return;
        }
        this.active = false;
        this.resetHandledTimeout();
        notifyReadEvent();
        ktucpListener.handleClose(this);
        //关闭之前尽量把消息都发出去
        notifyWriteEvent();
        //连接删除
        kcp.flush(false, this.kcpTimeService.now());
        channelManager.remove(this);
        //关闭channel
        closeChannel();
        release();
    }

    void closeChannel() {
        this.user().getUserNetManager().closeAllChannel();
    }

    void release() {
        kcp.setState(-1);
        kcp.release();
        for (; ; ) {
            Object object = writeObjectQueue.poll();
            if (object == null) {
                break;
            }
            ReferenceCountUtil.release(object);
        }
        for (; ; ) {
            ByteBuf byteBuf = readBufferQueue.poll();
            if (byteBuf == null) {
                break;
            }
            byteBuf.release();
        }
        if (this.fecEncode != null) {
            this.fecEncode.release();
        }

        if (this.fecDecode != null) {
            this.fecDecode.release();
        }
    }

    protected AtomicBoolean getWriteProcessing() {
        return writeProcessing;
    }

    protected AtomicBoolean getReadProcessing() {
        return readProcessing;
    }

    public IMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }

    public boolean isHandledTimeout() {
        return handledTimeout;
    }

    public void setHandledTimeout(boolean handledTimeout) {
        this.handledTimeout = handledTimeout;
    }

    protected long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void changeTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        // 修改超时，可再次处理超时
        this.resetHandledTimeout();
    }

    protected void resetHandledTimeout() {
        this.handledTimeout = false;
    }

    protected AtomicInteger getWriteBufferIncr() {
        return writeBufferIncr;
    }

    protected boolean isControlReadBufferSize() {
        return controlReadBufferSize;
    }

    protected boolean isControlWriteBufferSize() {
        return controlWriteBufferSize;
    }

    public IKtucpTimeService getKcpTimeService() {
        return kcpTimeService;
    }

    @SuppressWarnings("unchecked")
    public User user() {
        return (User) kcp.getUser();
    }

    public Uktucp user(User user) {
        kcp.setUser(user);
        return this;
    }

    @Override
    public String toString() {
        return "Ukcp(" +
                "getConv=" + kcp.getConv() +
                ", state=" + kcp.getState() +
                ", active=" + active +
                ')';
    }
}
