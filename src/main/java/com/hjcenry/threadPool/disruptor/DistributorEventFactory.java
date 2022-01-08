package com.hjcenry.threadPool.disruptor;

import com.lmax.disruptor.EventFactory;

public class DistributorEventFactory implements EventFactory<DistributorHandler> {

    @Override
    public DistributorHandler newInstance() {
        return new DistributorHandler();
    }

}
