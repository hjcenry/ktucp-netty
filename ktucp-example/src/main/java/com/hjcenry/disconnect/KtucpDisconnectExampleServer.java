package com.hjcenry.disconnect;

import com.hjcenry.kcp.ChannelConfig;
import com.hjcenry.net.server.KtucpServer;
import com.hjcenry.kcp.Uktucp;
import com.hjcenry.kcp.listener.SimpleKtucpListener;
import io.netty.buffer.ByteBuf;

/**
 * 重复新连接进入断开测试内存泄漏服务器
 * Created by JinMiao
 * 2019-06-27.
 */
public class KtucpDisconnectExampleServer extends SimpleKtucpListener<ByteBuf> {

    public static void main(String[] args) {

        KtucpDisconnectExampleServer kcpRttExampleServer = new KtucpDisconnectExampleServer();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndWnd(1024);
        channelConfig.setRcvWnd(1024);
        channelConfig.setMtu(1400);
        //channelConfig.setFecDataShardCount(10);
        //channelConfig.setFecParityShardCount(3);
        //channelConfig.setAckNoDelay(true);
        //channelConfig.setCrc32Check(true);
        channelConfig.setUseConvChannel(true);
        channelConfig.setTimeoutMillis(5000);
        KtucpServer ktucpServer = new KtucpServer();
        ktucpServer.init(kcpRttExampleServer, channelConfig, 10031);
    }


    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println("有连接进来 " + Thread.currentThread().getName() + uktucp.user().getUserNetManager().getRemoteSocketAddress(netId));
    }

    @Override
    protected void handleReceive0(ByteBuf buf, Uktucp uktucp) throws Exception {
        uktucp.write(buf);
    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleTimeout!!!:" + uktucp);
    }

    @Override
    public void handleException(Throwable ex, Uktucp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Uktucp kcp) {
        System.out.println("连接断开了 " + kcp.getConv());
    }
}