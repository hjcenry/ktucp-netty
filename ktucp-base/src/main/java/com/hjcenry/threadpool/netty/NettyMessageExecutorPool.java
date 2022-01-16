package com.hjcenry.threadpool.netty;

import com.hjcenry.threadpool.IMessageExecutor;
import com.hjcenry.threadpool.IMessageExecutorPool;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于netty eventloop的线程池
 * Created by JinMiao
 * 2020/11/24.
 */
public class NettyMessageExecutorPool implements IMessageExecutorPool {

    private EventLoopGroup eventExecutors;

    protected static final AtomicInteger INDEX = new AtomicInteger();

    public NettyMessageExecutorPool(int workSize) {
        eventExecutors = new DefaultEventLoopGroup(workSize, r -> {
            return new Thread(r, "nettyMessageExecutorPool-" + INDEX.incrementAndGet());
        });
    }

    @Override
    public IMessageExecutor getMessageExecutor() {
        return new NettyMessageExecutor(eventExecutors.next());
    }

    @Override
    public void stop() {
        if (!eventExecutors.isShuttingDown()) {
            eventExecutors.shutdownGracefully();
        }
    }
}
