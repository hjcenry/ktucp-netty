package com.hjcenry.net.client;

import com.hjcenry.exception.KcpInitException;
import com.hjcenry.net.INet;
import com.hjcenry.net.NetConfigData;
import com.hjcenry.net.client.tcp.TcpNetClient;
import com.hjcenry.net.client.udp.UdpNetClient;
import com.hjcenry.net.server.NetTypeEnum;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网络客户端工厂
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/8 17:45
 **/
public class NetClientFactory {

    public static INet createNetClient(int netId, NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KcpInitException {
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
