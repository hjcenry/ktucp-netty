package com.hjcenry.net.server;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.INet;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.kcp.NetTypeEnum;
import com.hjcenry.net.server.tcp.TcpNetServer;
import com.hjcenry.net.server.udp.UdpNetServer;

/**
 * 网络服务工厂
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:45
 **/
public class NetServerFactory {

    public static INet createNetServer(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        switch (netTypeEnum) {
            case NET_UDP:
                return new UdpNetServer(netId, netTypeEnum, netConfigData);
            case NET_TCP:
                return new TcpNetServer(netId, netTypeEnum, netConfigData);
            default:
                break;
        }
        return null;
    }
}
