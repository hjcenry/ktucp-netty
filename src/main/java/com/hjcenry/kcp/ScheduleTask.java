package com.hjcenry.kcp;

import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.ITask;
import com.hjcenry.time.IKcpTimeService;
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

    private final Ukcp ukcp;

    private final HashedWheelTimer hashedWheelTimer;

    private final IKcpTimeService kcpTimeService;

    private final boolean kcpIdleTimeoutClose;

    public ScheduleTask(IMessageExecutor messageExecutor, Ukcp ukcp, HashedWheelTimer hashedWheelTimer, boolean kcpIdleTimeoutClose) {
        this.messageExecutor = messageExecutor;
        this.ukcp = ukcp;
        this.kcpTimeService = ukcp.getKcpTimeService();
        this.hashedWheelTimer = hashedWheelTimer;
        this.kcpIdleTimeoutClose = kcpIdleTimeoutClose;
    }

    //flush策略
    //1,在send调用后检查缓冲区如果可以发送直接调用update得到时间并存在ukcp内
    //2,定时任务到了检查ukcp的时间和自己的定时 如果可以发送则直接发送  时间延后则重新定时
    //定时任务发送成功后检测缓冲区 是否触发发送时间
    //3,读时间触发后检测检测缓冲区触发写事件
    @Override
    public void execute() {
        try {
            final Ukcp ukcp = this.ukcp;
            long now = this.kcpTimeService.now();
            //判断连接是否关闭
            if (ukcp.getTimeoutMillis() != 0 && now - ukcp.getTimeoutMillis() > ukcp.getLastReceiveTime()) {
                ukcp.getKcpListener().handleIdleTimeout(ukcp);
                // 需要关闭连接
                if (kcpIdleTimeoutClose) {
                    ukcp.internalClose();
                }
            }
            if (!ukcp.isActive()) {
                return;
            }
            long timeLeft = ukcp.getTsUpdate() - now;
            //判断执行时间是否到了
            if (timeLeft > 0) {
                hashedWheelTimer.newTimeout(this, timeLeft, TimeUnit.MILLISECONDS);
                return;
            }
            long next = ukcp.flush(now);
            hashedWheelTimer.newTimeout(this, next, TimeUnit.MILLISECONDS);
            //检测写缓冲区 如果能写则触发写事件
            if (!ukcp.getWriteObjectQueue().isEmpty() && ukcp.canSend(false)) {
                ukcp.notifyWriteEvent();
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
