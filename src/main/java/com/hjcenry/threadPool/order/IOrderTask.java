package com.hjcenry.threadPool.order;

import com.hjcenry.threadPool.order.OrderedThreadSession;

/**
 * Created by JinMiao
 * 2020/6/19.
 */
public interface IOrderTask extends Runnable {

    OrderedThreadSession getSession();
}
