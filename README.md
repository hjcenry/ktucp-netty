# ktucp-netty

[![Powered][2]][1]

[1]: https://github.com/skywind3000/kcp

[2]: http://skywind3000.github.io/word/images/kcp.svg

[README in english](https://github.com/hjcenry/ktucp-netty/blob/master/README.en.md)

> **KTUCP实现：以KCP为应用层，TCP/UDP为多通道协议层**

`KCP是一个基于udp的快速可靠协议(udp)，能以比 TCP浪费



10%-20%的带宽的代价，换取平均延迟降低 30%-40%，且最大延迟降低三倍的传输效果。`

基于原作者的开源项目的修改：https://github.com/l42111996/java-Kcp.git

原项目：

**通信架构**

```
应用层 <--> UDP <--> KCP
```

**实现功能**

- java版kcp基本实现
- 优化kcp的flush策略
- 基于事件驱动，利用多核性能
- 支持配置多种kcp参数
- 支持配置conv或address(ip+port)确定唯一连接
- 支持fec（降低延迟）
- 支持crc32校验

基于原项目的新增和优化：

**通信架构**
```
 应用层
  ┌┴┐          
UDP TCP  ...(N个网络)
  └┬┘
  KCP
```

**优化和新增**

- 支持配置多个TCP/UDP底层网络服务
- 支持TCP和UDP通道切换
- 支持自定义配置底层网络的Netty参数
- 支持添加底层网络的自定义Handler
- 支持自定义编解码
- 支持切换KCP下层的网络
- 支持强制使用某一个网络发送数据
- 支持使用自定义时间服务（可以不用System.currentTimeMillis方法而使用自己系统的缓存时间系统）

# 为什么要使用多网络

根据[原作者对KCP的使用建议](https://github.com/skywind3000/kcp/wiki/Cooperate-With-Tcp-Server)
实际使用中，最好是通过TCP和UDP结合的方式使用：
1. 中国网络情况特殊，可能出现UDP包被防火墙拦下
2. TCP网络在使用LB的情况下，两端中的一端可能出现感知不到对方断开的情况
3. 可通过TCP的可靠连接作为备用线路，UDP不通的情况下可使用备用TCP

结合以上需求，**这套开源库的目的就是整合TCP和UDP网络到同一套KCP机制中，甚至可以支持启动多TCP多UDP服务。**
并且最大程度的开放底层Netty配置权限，用户可根据自己的需求定制化自己的网络框架

`欢迎大家使用，有任何bug以及优化需求，欢迎提issue讨论`

# 快速开始

# maven地址
```xml
<dependency>
    <groupId>io.github.hjcenry</groupId>
    <artifactId>ktucp-net</artifactId>
    <version>1.1</version>
</dependency>
```

## 服务端

## 1. 创建ChannelConfig
```java
ChannelConfig channelConfig = new ChannelConfig();
channelConfig.nodelay(true, 40, 2, true);
channelConfig.setSndWnd(512);
channelConfig.setRcvWnd(512);
channelConfig.setMtu(512);
channelConfig.setTimeoutMillis(10000);
channelConfig.setUseConvChannel(true);
// 这里可以配置大部分的参数
// ...
```

## 2. 创建KtucpListener监听网络事件
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

## 3. 创建并启动KtcupServer
```java
KtucpServer ktucpServer = new KtucpServer();
// 默认启动一个UDP端口
ktucpServer.init(ktucpListener, channelConfig, 8888);
```

## 4. 观察日志
```java
[main] INFO com.hjcenry.log.KtucpLog - KtucpServer Start :
===========================================================
TcpNetServer{bindPort=8888, bossGroup.num=1, ioGroup.num=8}
UdpNetServer{bindPort=8888, bossGroup.num=8, ioGroup.num=0}
===========================================================
```


## 客户端

## 1. 创建ChannelConfig
```java
ChannelConfig channelConfig = new ChannelConfig();
// 客户端比服务端多一个设置convId
channelConfig.setConv(1);
channelConfig.nodelay(true, 40, 2, true);
channelConfig.setSndWnd(512);
channelConfig.setRcvWnd(512);
channelConfig.setMtu(512);
channelConfig.setTimeoutMillis(10000);
channelConfig.setUseConvChannel(true);
// 这里可以配置大部分的参数
// ...
```

## 2. 创建KtucpListener监听网络事件
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

## 3. 创建并启动KtcupClient
```java
// 默认启动一个UDP端口
KtucpClient ktucpClient = new KtucpClient();
ktucpClient.init(ktucpListener, channelConfig, new InetSocketAddress("127.0.0.1", 8888));
```

## 4. 观察日志
```java
[main] INFO com.hjcenry.log.KtucpLog - KtucpClient Connect :
===========================================================
TcpNetClient{connect= local:null -> remote:/127.0.0.1:8888, ioGroup.num=8}
UdpNetClient{connect= local:null -> remote:/127.0.0.1:8888, ioGroup.num=0}
===========================================================
```

> `以上是简单的示例，可快速启动ktucp服务和客户端。关于多网络的详细使用方法，可参考下面的例子3和4`

# 使用注意

- **客户端实现**：该框架仅实现了Java版本，其他版本的客户端需要根据此通信架构进行实现（单纯使用UDP通道的话，也是能和原版KCP兼容的）
- **convId的唯一性**：因不能校验udp的address或tcp的channel，只能依靠convId获取唯一Uktucp对象
- **convId的有效性校验**：需要判断convId的来源，防止伪造。因convId从消息包读取，框架底层对TCP连接的消息包做了Channel唯一性判断处理，但UDP暂时没有好的判断方法。如果有安全性需求，应用层需要自己做一个防伪检测，比如服务端给客户端分配一个token，客户端在每个消息包头把token带过来，服务端对每个包头的token做一个校验
- **处理好多网络连接管理**：因底层配置较为开放，默认为KCP超时即断开所有连接，如有其他配置，请注意连接释放时机

# 使用方法以及例子
1. [server端示例](https://github.com/hjcenry/ktucp-netty/ketucp-example/src/main/test/KcpRttExampleServer.java)
2. [client端实例](https://github.com/hjcenry/ktucp-netty/ketucp-example/src/main/test/KcpRttExampleClient.java)
3. [多网络server端示例](https://github.com/hjcenry/ktucp-netty/ketucp-example/src/main/test/KcpMultiNetExampleServer.java)
4. [多网络client端实例](https://github.com/hjcenry/ktucp-netty/ketucp-example/src/main/test/KcpMultiNetExampleClient.java)
5. [最佳实践](https://github.com/skywind3000/kcp/wiki/KCP-Best-Practice)
6. [大量资料](https://github.com/skywind3000/kcp)
7. [C#兼容版服务端](https://github.com/hjcenry/ktucp-netty/blob/master/kcp-example/src/main/java/test/Kcp4sharpExampleServer.java) 
8. [C#客户端](https://github.com/l42111996/csharp-kcp/blob/master/example-Kcp/KcpRttExampleClient.cs)
9. [兼容kcp-go](https://github.com/l42111996/java-Kcp/blob/master/kcp-example/src/main/java/test/Kcp4GoExampleClient.java)

# 相关资料

1. https://github.com/skywind3000/kcp 原版c版本的kcp
2. https://github.com/xtaci/kcp-go go版本kcp,有大量优化
3. https://github.com/Backblaze/JavaReedSolomon java版本fec
4. https://github.com/LMAX-Exchange/disruptor 高性能的线程间消息传递库
5. https://github.com/JCTools/JCTools 高性能并发库
6. https://github.com/szhnet/kcp-netty java版本的一个kcp
7. https://github.com/l42111996/csharp-kcp 基于dotNetty的c#版本kcp,完美兼容
8. https://github.com/l42111996/java-Kcp.git 此开源库的原版本

# 交流

- 微信:hjcenry