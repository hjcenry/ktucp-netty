package com.hjcenry.threadPool.disruptor;

import com.lmax.disruptor.EventHandler;

public class DistributorEventHandler implements EventHandler<DistributorHandler> {

    @Override
    public void onEvent(DistributorHandler event, long sequence,
                        boolean endOfBatch) {
        event.execute();
    }
}
