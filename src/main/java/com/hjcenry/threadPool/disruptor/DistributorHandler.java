package com.hjcenry.threadPool.disruptor;

import com.hjcenry.log.KtucpLog;
import com.hjcenry.threadPool.ITask;
import org.slf4j.Logger;

public class DistributorHandler {

    protected static final Logger logger = KtucpLog.logger;
    private ITask task;

    public void execute() {
        try {
            this.task.execute();
            //得主动释放内存
            this.task = null;
        } catch (Throwable throwable) {
            logger.error("error", throwable);
        }
    }

    public void setTask(ITask task) {
        this.task = task;
    }
}
