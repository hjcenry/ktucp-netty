package com.hjcenry.server;

import com.hjcenry.system.SystemOS;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty事件组和管道对应关系
 *
 * @ClassName NettyGroupChannel
 * @Author hejincheng
 * @Date 2021/12/28 15:26
 * @Version 1.0
 **/
public enum NettyGroupChannel {
    /**
     * Window系統
     */
    WINDOWS("windows") {
        @Override
        public EventLoopGroup createEventLoopGroup(int threadNum) {
            return new NioEventLoopGroup(threadNum);
        }

        @Override
        public Class<? extends Channel> getUdpChannelClass() {
            return NioDatagramChannel.class;
        }

        @Override
        public Class<? extends ServerChannel> getTcpChannelClass() {
            return NioServerSocketChannel.class;
        }
    },
    /**
     * Linux系統
     */
    LINUX("linux") {
        @Override
        public EventLoopGroup createEventLoopGroup(int threadNum) {
            if (SystemOS.EPOLL_AVAILABLE) {
                return new EpollEventLoopGroup(threadNum);
            }
            return new EpollEventLoopGroup(threadNum);
        }

        @Override
        public Class<? extends Channel> getUdpChannelClass() {
            if (SystemOS.EPOLL_AVAILABLE) {
                return EpollDatagramChannel.class;
            }
            return NioDatagramChannel.class;
        }

        @Override
        public Class<? extends ServerChannel> getTcpChannelClass() {
            if (SystemOS.EPOLL_AVAILABLE) {
                return EpollServerSocketChannel.class;
            }
            return NioServerSocketChannel.class;
        }

    },
    /**
     * Mac系統
     */
    MAX_OS("mac os") {
        @Override
        public EventLoopGroup createEventLoopGroup(int threadNum) {
            if (SystemOS.KQUEUE_AVAILABLE) {
                return new KQueueEventLoopGroup(threadNum);
            }
            return new NioEventLoopGroup(threadNum);
        }

        @Override
        public Class<? extends Channel> getUdpChannelClass() {
            if (SystemOS.KQUEUE_AVAILABLE) {
                return KQueueDatagramChannel.class;
            }
            return NioDatagramChannel.class;
        }

        @Override
        public Class<? extends ServerChannel> getTcpChannelClass() {
            if (SystemOS.KQUEUE_AVAILABLE) {
                return KQueueServerSocketChannel.class;
            }
            return NioServerSocketChannel.class;
        }
    },
    ;

    private String osName;

    NettyGroupChannel(String osName) {
        this.osName = osName;
    }

    /**
     * 获取当前操作系统对应的Netty对应关系
     *
     * @return 当前操作系统对应的Netty对应关系
     */
    public static NettyGroupChannel getNettyGroupChannel() {
        if (SystemOS.isLinux()) {
            return NettyGroupChannel.LINUX;
        } else if (SystemOS.isMac()) {
            return NettyGroupChannel.MAX_OS;
        } else {
            return NettyGroupChannel.WINDOWS;
        }
    }

    /**
     * 创建事件组
     *
     * @return
     */
    public abstract EventLoopGroup createEventLoopGroup(int threadNum);

    /**
     * 获取UDP管道类
     *
     * @return
     */
    public abstract Class<? extends Channel> getUdpChannelClass();

    /**
     * 获取TCP管道类
     *
     * @return
     */
    public abstract Class<? extends ServerChannel> getTcpChannelClass();

}
