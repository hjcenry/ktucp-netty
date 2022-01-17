# ktucp-netty

[![Powered][2]][1]

[1]: https://github.com/skywind3000/kcp
[2]: http://skywind3000.github.io/word/images/kcp.svg

[中文文档](https://github.com/hjcenry/ktucp-netty/blob/master/README.md)

> **KTUCP implementation: KCP as the application layer, TCP/UDP multi-channel protocol layer**

`KCP is a udp-based fast and reliable protocol (udp), which can reduce the average delay by 30% -40% at the cost of wasting 10% -20% of bandwidth over TCP, and reduce the maximum delay by three times the transmission effect.`

Modifications based on the original author's open source project:https://github.com/l42111996/java-Kcp.git

Original projects:

**Communication architecture**

```
Application <--> UDP <--> KCP
```

**Function realization**

- Java version of KCP basic implementation
- Optimize flush policy for KCP
- Based on event-driven, make full use of multi-core
- You can configure multiple KCP parameters
- You can configure conv or address(IP +port) to determine unique connections
- Include fec to reduce latency
- With crc32 check

New additions and optimizations based on original projects:

**Communication architecture**
```
 Application
     ┌┴┐          
   UDP TCP  ...(several nets)
     └┬┘
     KCP
```

**Optimizations and additions**

- Multiple TCP/UDP underlying network services can be configured
- Supports TCP and UDP channel switching
- You can customize Netty parameters for underlying networks
- Supports adding custom handlers for the underlying network
- Supports custom codec
- Supports switching the network under the KCP layer
- Support to force a network to send data
- Support for custom time services (you can use your own System's cache time System instead of using the System.CurrentTimemillis method)

# Why use multiple networks为什么要使用多网络

According to[Origin author's recommendations on the use of KCP](https://github.com/skywind3000/kcp/wiki/Cooperate-With-Tcp-Server)
In practice, it is best to use TCP and UDP:
1. The Chinese network is special, and UDP packets may be blocked by the firewall
2. When LB is used on the TCP network, one end of the TCP network may fail to detect the connection of the other end
3. A reliable TCP connection can be used as the standby line. When UDP fails, the standby TCP connection can be used

Combined with the above requirements,**The purpose of this open source library is to integrate TCP and UDP networks into the same KCP mechanism, and even enable multiple TCP and UDP services.**
In addition, the basic Netty configuration permission is open to the maximum extent, and users can customize their own network framework according to their own requirements

`Welcome to use, there are any bugs and optimization requirements, welcome to commit issues to discuss`

# Quick Start

# Maven Repository
```xml
<dependency>
    <groupId>io.github.hjcenry</groupId>
    <artifactId>ktucp-net</artifactId>
    <version>1.1</version>
</dependency>
```

## Server

## 1. create ChannelConfig
```java
ChannelConfig channelConfig = new ChannelConfig();
channelConfig.nodelay(true, 40, 2, true);
channelConfig.setSndWnd(512);
channelConfig.setRcvWnd(512);
channelConfig.setMtu(512);
channelConfig.setTimeoutMillis(10000);
channelConfig.setUseConvChannel(true);
// you can configure most here
// ...
```

## 2. create KtucpListener to listener the net event
```java
KtucpListener ktucpListener = new KtucpListener() {
    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println("onConnected:" + uktucp);
    }

    @Override
    public void handleReceive(Object object, Uktucp uktucp) throws Exception {
        System.out.println("handleReceive:" + uktucp);
        ByteBuf byteBuf = (ByteBuf) object;
        // TODO read byteBuf
    }

    @Override
    public void handleException(Throwable ex, Uktucp uktucp) {
        System.out.println("handleException:" + uktucp);
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Uktucp uktucp) {
        System.out.println("handleClose:" + uktucp);
        System.out.println("snmp:" + uktucp.getSnmp());
    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleIdleTimeout:" + uktucp);
    }
};
```

## 3. create and init KtcupServer
```java
KtucpServer ktucpServer = new KtucpServer();
// start a UDP port for default
ktucpServer.init(ktucpListener, channelConfig, 8888);
```

## 4. watch log
```java
[main] INFO com.hjcenry.log.KtucpLog - KtucpServer Start :
===========================================================
TcpNetServer{bindPort=8888, bossGroup.num=1, ioGroup.num=8}
UdpNetServer{bindPort=8888, bossGroup.num=8, ioGroup.num=0}
===========================================================
```


## Client

## 1. create ChannelConfig
```java
ChannelConfig channelConfig = new ChannelConfig();
// the client has a parameter convId more than server
channelConfig.setConv(1);
channelConfig.nodelay(true, 40, 2, true);
channelConfig.setSndWnd(512);
channelConfig.setRcvWnd(512);
channelConfig.setMtu(512);
channelConfig.setTimeoutMillis(10000);
channelConfig.setUseConvChannel(true);
// you can configure most here
// ...
```

## 2. create KtucpListener to listener the net event 
```java
KtucpListener ktucpListener = new KtucpListener() {
    @Override
    public void onConnected(int netId, Uktucp uktucp) {
        System.out.println("onConnected:" + uktucp);
    }

    @Override
    public void handleReceive(Object object, Uktucp uktucp) throws Exception {
        System.out.println("handleReceive:" + uktucp);
        ByteBuf byteBuf = (ByteBuf) object;
        // TODO read byteBuf
    }

    @Override
    public void handleException(Throwable ex, Uktucp uktucp) {
        System.out.println("handleException:" + uktucp);
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Uktucp uktucp) {
        System.out.println("handleClose:" + uktucp);
        System.out.println("snmp:" + uktucp.getSnmp());
    }

    @Override
    public void handleIdleTimeout(Uktucp uktucp) {
        System.out.println("handleIdleTimeout:" + uktucp);
    }
};
```

## 3. create and init KtcupClient
```java
// start a UDP port for default
KtucpClient ktucpClient = new KtucpClient();
ktucpClient.init(ktucpListener, channelConfig, new InetSocketAddress("127.0.0.1", 8888));
```

## 4. watch log
```java
[main] INFO com.hjcenry.log.KtucpLog - KtucpClient Connect :
===========================================================
TcpNetClient{connect= local:null -> remote:/127.0.0.1:8888, ioGroup.num=8}
UdpNetClient{connect= local:null -> remote:/127.0.0.1:8888, ioGroup.num=0}
===========================================================
```

# Use caution

- **ConvId uniqueness** : Can not verify udp address or TCP channel, only rely on convId to obtain a unique Uktucp object
- **Verify the validity of convId** : To determine the source of convId, you can determine whether the UDP adress or TCP channel is the same as the last source (this method needs to pay attention to handling the disconnection during 4G and wifi switching).
- **Handle a lot of network connection management** : because the underlying configuration is more open, the default is KCP timeout, that is, disconnect all connections, if there are other configurations, please note the connection release time

> `The above is a simple example to quickly start the KTUCP service and client. For details about how to use multiple networks, see Examples 3 and 4 below`

# Use methods and examples
1. [The server end sample](https://github.com/hjcenry/ktucp-netty/blob/master/ketucp-example/src/main/test/KcpRttExampleServer.java)
2. [The client end sample](https://github.com/hjcenry/ktucp-netty/blob/master/ketucp-example/src/main/test/KcpRttExampleClient.java)
3. [Multi-network Server example](https://github.com/hjcenry/ktucp-netty/blob/master/ketucp-example/src/main/test/KcpMultiNetExampleServer.java)
4. [Multi-network client example](https://github.com/hjcenry/ktucp-netty/blob/master/ketucp-example/src/main/test/KcpMultiNetExampleClient.java)
5. [Best practices](https://github.com/skywind3000/kcp/wiki/KCP-Best-Practice)
6. [A large number of data](https://github.com/skywind3000/kcp)
7. [C# compatible server](https://github.com/hjcenry/ktucp-netty/blob/master/kcp-example/src/main/java/test/Kcp4sharpExampleServer.java) , 
8. [C# client](https://github.com/l42111996/csharp-kcp/blob/master/example-Kcp/KcpRttExampleClient.cs)
9. [Compatible with kcp-go](https://github.com/hjcenry/ktucp-netty/blob/master/kcp-example/src/main/java/test/Kcp4GoExampleClient.java)

# The relevant data

1. https://github.com/skywind3000/kcp Original C version of KCP
2. https://github.com/xtaci/kcp-go The GO version, KCP with a lot of optimizations
3. https://github.com/Backblaze/JavaReedSolomon Java version of fec
4. https://github.com/LMAX-Exchange/disruptor High performance interthread messaging library
5. https://github.com/JCTools/JCTools High performance concurrent library
6. https://github.com/szhnet/kcp-netty Java version of a KCP
7. https://github.com/l42111996/csharp-kcp DotNetty based c# version of KCP, perfect compatibility
8. https://github.com/l42111996/java-Kcp.git The original version of this open source library

# Communication

- wechat:hjcenry