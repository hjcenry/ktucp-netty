package com.hjcenry.threadpool.order;

/**
 * Created by JinMiao
 * 2020/6/19.
 */
public interface IOrderTask extends Runnable {

    OrderedThreadSession getSession();
}
