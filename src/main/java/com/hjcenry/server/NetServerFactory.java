package com.hjcenry.server;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.server.tcp.TcpNetServer;
import com.hjcenry.server.udp.UdpNetServer;

/**
 * 网络服务工厂
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:45
 **/
public class NetServerFactory {

    public static INetServer createNetServer(NetServerEnum serverEnum, NetConfigData netConfigData) throws KcpInitException {
        switch (serverEnum) {
            case NET_UDP:
                return new UdpNetServer(netConfigData);
            case NET_TCP:
                return new TcpNetServer(netConfigData);
            default:
                break;
        }
        return null;
    }
}
