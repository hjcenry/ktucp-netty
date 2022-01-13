package com.hjcenry.system;

import com.hjcenry.log.KcpLog;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class SystemOS {

    private static final Logger logger = KcpLog.logger;

    public static String osName;
    /**
     * Epoll可用
     */
    public static final boolean EPOLL_AVAILABLE = Epoll.isAvailable();
    /**
     * Kqueue可用
     */
    public static final boolean KQUEUE_AVAILABLE = KQueue.isAvailable();
    /**
     * CPU数量
     */
    public static final int CPU_NUM;

    static {
        osName = System.getProperties().getProperty("os.name");
        CPU_NUM = Runtime.getRuntime().availableProcessors();
    }

    public static boolean isWindows() {
        return osName.toLowerCase().startsWith("window");
    }

    public static boolean isLinux() {
        return osName.toLowerCase().startsWith("linux");
    }

    public static boolean isMac() {
        return osName.toLowerCase().startsWith("mac");
    }

    /**
     * 获取内网IP
     *
     * @return 内网IP
     */
    public static String getInternalIP() throws UnknownHostException {
        InetAddress inet;
        if (logger.isInfoEnabled()) {
            logger.info("当前操作系统 : " + osName);
        }
        if (isWindows()) {
            inet = getWinLocalIp();
            // 针对linux系统
        } else {
            inet = getLinuxLocalIp();
        }
        if (null == inet) {
            throw new UnknownHostException("主机的ip地址未知");
        }
        // 获得本机IP
        return inet.getHostAddress().replace("\\", "");
    }

    private static InetAddress getWinLocalIp() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    private static InetAddress getLinuxLocalIp() {
        // 定义网络接口枚举类
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            // 获得网络接口
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            // 声明一个InetAddress类型ip地址
            InetAddress ip;
            // 遍历所有的网络接口
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // 打印网端名字
                logger.info(netInterface.getName());
                // 同样再定义网络地址枚举类
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    logger.info(ip.getHostAddress());
                    if (ip instanceof Inet4Address) {
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("", e);
        }
        return null;
    }

}
