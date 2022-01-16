package com.hjcenry.net.client;

import com.hjcenry.exception.KtucpInitException;
import com.hjcenry.kcp.NetTypeEnum;
import com.hjcenry.kcp.INet;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.client.tcp.TcpNetClient;
import com.hjcenry.net.client.udp.UdpNetClient;

/**
 * 网络客户端工厂
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:45
 **/
public class NetClientFactory {

    public static INet createNetClient(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KtucpInitException {
        switch (netTypeEnum) {
            case NET_UDP:
                return new UdpNetClient(netId, netTypeEnum, netConfigData);
            case NET_TCP:
                return new TcpNetClient(netId, netTypeEnum, netConfigData);
            default:
                break;
        }
        return null;
    }
}
