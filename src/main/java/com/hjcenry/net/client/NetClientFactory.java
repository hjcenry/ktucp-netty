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

    /**
     * 网络服务ID
     */
    static AtomicInteger netId = new AtomicInteger(0);

    public static INet createNetClient(NetTypeEnum netTypeEnum, NetConfigData netConfigData) throws KcpInitException {
        int id = netId.incrementAndGet();
        switch (netTypeEnum) {
            case NET_UDP:
                return new UdpNetClient(id, netTypeEnum, netConfigData);
            case NET_TCP:
                return new TcpNetClient(id, netTypeEnum, netConfigData);
            default:
                break;
        }
        return null;
    }
}
