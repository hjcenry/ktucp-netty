package com.hjcenry.server;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.server.tcp.TcpNetServer;
import com.hjcenry.server.udp.UdpNetServer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网络服务工厂
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:45
 **/
public class NetServerFactory {

    /**
     * 网络服务ID
     */
    static AtomicInteger netId = new AtomicInteger(0);

    public static INetServer createNetServer(NetServerEnum serverEnum, NetConfigData netConfigData) throws KcpInitException {
        int id = netId.incrementAndGet();
        switch (serverEnum) {
            case NET_UDP:
                return new UdpNetServer(id, netConfigData);
            case NET_TCP:
                return new TcpNetServer(id, netConfigData);
            default:
                break;
        }
        return null;
    }
}
