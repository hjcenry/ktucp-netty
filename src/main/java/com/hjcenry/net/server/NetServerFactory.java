package com.hjcenry.net.server;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.net.INet;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.server.tcp.TcpNetServer;
import com.hjcenry.net.server.udp.UdpNetServer;

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

    public static INet createNetServer(NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KcpInitException {
        int id = netId.incrementAndGet();
        switch (netTypeEnum) {
            case NET_UDP:
                return new UdpNetServer(id, netTypeEnum, netConfigData);
            case NET_TCP:
                return new TcpNetServer(id, netTypeEnum, netConfigData);
            default:
                break;
        }
        return null;
    }
}
