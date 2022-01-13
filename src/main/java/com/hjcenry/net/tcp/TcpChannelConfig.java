package com.hjcenry.net.tcp;

import com.hjcenry.net.NetChannelConfig;
import com.hjcenry.net.server.NetTypeEnum;
import io.netty.channel.ChannelOption;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * TCP网络配置
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 17:06
 **/
public class TcpChannelConfig extends NetChannelConfig {

    //================================================
    //TCP超时参数start
    //================================================
    /**
     * 写超时，默认无超时，单位(ms)
     */
    private long readIdleTime = 0;
    /**
     * 读超时，默认无超时，单位(ms)
     */
    private long writeIdleTime = 0;
    /**
     * 全部超时，默认无超时，单位(ms)
     */
    private long allIdleTime = 0;
    //================================================
    //TCP超时参数end
    //================================================

    //================================================
    //TCP粘包拆包参数start(如使用LengthFieldBasedFrameDecoder和LengthFieldPrepender，需要配置以下参数，或使用默认值)
    //================================================
    /**
     * 发送的数据帧最大长度
     */
    private int maxFrameLength = Integer.MAX_VALUE;
    /**
     * 定义长度域位于发送的字节数组中的下标。换句话说：发送的字节数组中下标为${lengthFieldOffset}的地方是长度域的开始地方
     */
    private int lengthFieldOffset = 0;
    /**
     * 用于描述定义的长度域的长度。换句话说：发送字节数组bytes时, 字节数组bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength]域对应于的定义长度域部分
     */
    private int lengthFieldLength = 4;
    /**
     * 满足公式: 发送的字节数组bytes.length - lengthFieldLength = bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength] + lengthFieldOffset + lengthAdjustment
     */
    private int lengthAdjustment = 0;
    /**
     * 接收到的发送数据包，去除前initialBytesToStrip位
     */
    private int initialBytesToStrip = 4;

    //================================================
    //TCP粘包拆包参数end
    //================================================

    /**
     * TCP服务Channel参数
     */
    private final Map<ChannelOption, Object> serverChannelOptions = new HashMap<>();
    /**
     * TCP客户端Channel参数
     */
    private final Map<ChannelOption, Object> childChannelOptions = new HashMap<>();

    protected TcpChannelConfig() {
    }

    /**
     * 构建服务端配置
     *
     * @param bindPort 绑定端口
     * @return 服务端配置
     */
    public static TcpChannelConfig buildServerConfig(int bindPort) {
        TcpChannelConfig channelConfig = new TcpChannelConfig();
        channelConfig.netTypeEnum = NetTypeEnum.NET_TCP;
        channelConfig.bindPort = bindPort;
        return channelConfig;
    }

    /**
     * 构建客户端配置
     *
     * @param serverAddress 远端地址
     * @return 客户端配置
     */
    public static TcpChannelConfig buildClientConfig(InetSocketAddress serverAddress) {
        return buildClientConfig(null, serverAddress);
    }

    /**
     * 构建客户端配置
     *
     * @param localAddress  本地地址
     * @param serverAddress 远端地址
     * @return 客户端配置
     */
    public static TcpChannelConfig buildClientConfig(InetSocketAddress localAddress, InetSocketAddress serverAddress) {
        TcpChannelConfig channelConfig = new TcpChannelConfig();
        channelConfig.netTypeEnum = NetTypeEnum.NET_TCP;
        channelConfig.clientConnectLocalAddress = localAddress;
        channelConfig.clientConnectRemoteAddress = serverAddress;
        return channelConfig;
    }

    /**
     * 添加服务端Channel配置
     *
     * @param channelOption 参数Key
     * @param t             参数值
     */
    public <T> void addTcpServerChannelOption(ChannelOption<T> channelOption, T t) {
        this.serverChannelOptions.put(channelOption, t);
    }

    /**
     * 添加Child端Channel配置
     *
     * @param channelOption 参数Key
     * @param t             参数值
     */
    public <T> void addTcpChildChannelOption(ChannelOption<T> channelOption, T t) {
        this.childChannelOptions.put(channelOption, t);
    }

    public Map<ChannelOption, Object> getServerChannelOptions() {
        return serverChannelOptions;
    }

    public Map<ChannelOption, Object> getChildChannelOptions() {
        return childChannelOptions;
    }


    public long getReadIdleTime() {
        return readIdleTime;
    }

    public void setReadIdleTime(long readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    public long getWriteIdleTime() {
        return writeIdleTime;
    }

    public void setWriteIdleTime(long writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
    }

    public long getAllIdleTime() {
        return allIdleTime;
    }

    public void setAllIdleTime(long allIdleTime) {
        this.allIdleTime = allIdleTime;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public int getLengthFieldOffset() {
        return lengthFieldOffset;
    }

    public void setLengthFieldOffset(int lengthFieldOffset) {
        this.lengthFieldOffset = lengthFieldOffset;
    }

    public int getLengthFieldLength() {
        return lengthFieldLength;
    }

    public void setLengthFieldLength(int lengthFieldLength) {
        this.lengthFieldLength = lengthFieldLength;
    }

    public int getLengthAdjustment() {
        return lengthAdjustment;
    }

    public void setLengthAdjustment(int lengthAdjustment) {
        this.lengthAdjustment = lengthAdjustment;
    }

    public int getInitialBytesToStrip() {
        return initialBytesToStrip;
    }

    public void setInitialBytesToStrip(int initialBytesToStrip) {
        this.initialBytesToStrip = initialBytesToStrip;
    }
}
