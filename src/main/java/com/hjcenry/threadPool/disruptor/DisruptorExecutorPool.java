package com.hjcenry.threadPool.disruptor;

import com.hjcenry.log.KtucpLog;
import com.hjcenry.threadPool.IMessageExecutor;
import com.hjcenry.threadPool.IMessageExecutorPool;
import org.slf4j.Logger;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于disruptor的线程池
 *
 * @author jinmiao
 * 2014-9-12 上午9:51:09
 */
public class DisruptorExecutorPool implements IMessageExecutorPool {
    private static final Logger logger = KtucpLog.logger;

    protected List<IMessageExecutor> executor = new Vector<>();

    protected AtomicInteger index = new AtomicInteger();


    public DisruptorExecutorPool(int workSize) {
        for (int i = 0; i < workSize; i++) {
            createDisruptorProcessor("DisruptorExecutorPool-" + i);
        }
    }


    /**
     * 创造一个线程对象
     *
     * @param threadName
     * @return
     */
    private IMessageExecutor createDisruptorProcessor(String threadName) {
        DisruptorSingleExecutor singleProcess = new DisruptorSingleExecutor(threadName);
        executor.add(singleProcess);
        singleProcess.start();
        return singleProcess;
    }


    @Override
    public void stop() {
        for (IMessageExecutor process : executor) {
            process.stop();
        }

        //if(!scheduled.isShutdown())
        //	scheduled.shutdown();
    }


    /**
     * 从线程池中按算法获得一个线程对象
     *
     * @return
     */
    @Override
    public IMessageExecutor getMessageExecutor() {
        int index = this.index.incrementAndGet();
        return executor.get(index % executor.size());
    }

}
