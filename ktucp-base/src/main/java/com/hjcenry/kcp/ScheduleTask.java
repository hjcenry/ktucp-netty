package com.hjcenry.kcp;

import com.hjcenry.threadpool.IMessageExecutor;
import com.hjcenry.threadpool.ITask;
import com.hjcenry.time.IKtucpTimeService;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

/**
 * Created by JinMiao
 * 2018/10/24.
 */
public class ScheduleTask implements ITask, Runnable, TimerTask {

    private final IMessageExecutor messageExecutor;

    private final Uktucp uktucp;

    private final HashedWheelTimer hashedWheelTimer;

    private final IKtucpTimeService kcpTimeService;

    private final boolean kcpIdleTimeoutClose;

    public ScheduleTask(IMessageExecutor messageExecutor, Uktucp uktucp, HashedWheelTimer hashedWheelTimer, boolean kcpIdleTimeoutClose) {
        this.messageExecutor = messageExecutor;
        this.uktucp = uktucp;
        this.kcpTimeService = uktucp.getKcpTimeService();
        this.hashedWheelTimer = hashedWheelTimer;
        this.kcpIdleTimeoutClose = kcpIdleTimeoutClose;
    }

    /**
     * 执行flush
     * flush策略
     * 1,在send调用后检查缓冲区如果可以发送直接调用update得到时间并存在uktucp内
     * 2,定时任务到了检查uktucp的时间和自己的定时，如果可以发送则直接发送，时间延后则重新定时，定时任务发送成功后检测缓冲区 是否触发发送时间
     * 3,读事件触发后检测检测缓冲区触发写事件
     */
    @Override
    public void execute() {
        try {
            final Uktucp uktucp = this.uktucp;
            long now = this.kcpTimeService.now();
            //判断连接是否关闭
            if (uktucp.getTimeoutMillis() != 0 && now - uktucp.getTimeoutMillis() > uktucp.getLastReceiveTime()) {
                if (!uktucp.isHandledTimeout()) {
                    // 仅处理一次超时接口
                    uktucp.getKcpListener().handleIdleTimeout(uktucp);
                    uktucp.setHandledTimeout(true);
                }
                // 需要关闭连接
                if (kcpIdleTimeoutClose) {
                    uktucp.internalClose();
                }
            }
            if (!uktucp.isActive()) {
                return;
            }
            long timeLeft = uktucp.getTsUpdate() - now;
            //判断执行时间是否到了
            if (timeLeft > 0) {
                hashedWheelTimer.newTimeout(this, timeLeft, TimeUnit.MILLISECONDS);
                return;
            }
            long next = uktucp.flush(now);
            hashedWheelTimer.newTimeout(this, next, TimeUnit.MILLISECONDS);
            //检测写缓冲区 如果能写则触发写事件
            if (!uktucp.getWriteObjectQueue().isEmpty() && uktucp.canSend(false)) {
                uktucp.notifyWriteEvent();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.messageExecutor.execute(this);
    }

    @Override
    public void run(Timeout timeout) {
        run();
    }
}
