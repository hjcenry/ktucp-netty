package com.hjcenry.threadpool.order.waitestrategy;

/**
 * 等待条件
 *
 * @param <T>
 */
public interface WaitCondition<T> {

    /**
     * 附件
     *
     * @return
     */
    T getAttach();

}
