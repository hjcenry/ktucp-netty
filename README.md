# ktucp-netty

[![Powered][2]][1]

[1]: https://github.com/skywind3000/kcp
[2]: http://skywind3000.github.io/word/images/kcp.svg

[README in english](https://github.com/hjcenry/ktucp-netty/README.en.md)

> **基于TCP/UDP为通信协议，以KCP实现可靠通信的双通道协议栈实现**

- 基于https://github.com/l42111996/java-Kcp.git的大部分实现
- 对代码使用中不方便的地方进行接口改造
- 加入TCP和UDP，并通过配置灵活选择
- 加入TCP和UDP通道切换机制


> 原项目是基于netty版本实现的kcp(包含fec功能的实现)

KCP是一个基于udp的快速可靠协议(udp)，能以比 TCP浪费10%-20%的带宽的代价，换取平均延迟降低 30%-40%，且最大延迟降低三倍的传输效果。

# maven地址:

```java
// TODO
```

`以下文档引用原作者的README`

> # 使用方法以及参数
> 1. [server端示例](https://github.com/hjcenry/ktucp-netty/src/main/test/KcpRttExampleServer.java)
> 2. [client端实例](https://github.com/hjcenry/ktucp-netty/src/main/test/KcpRttExampleClient.java)
> 3. [最佳实践](https://github.com/skywind3000/kcp/wiki/KCP-Best-Practice)
> 4. [大量资料](https://github.com/skywind3000/kcp)
> 5. 兼容c#端，[java服务端](https://github.com/l42111996/java-Kcp/blob/master/kcp-example/src/main/java/test/Kcp4sharpExampleServer.java) , [c#客户端](https://github.com/l42111996/csharp-kcp/blob/master/example-Kcp/KcpRttExampleClient.cs)
> 6. [遇到过的问题](https://github.com/l42111996/java-Kcp/blob/master/QA.md)
> 7. [性能测试结果](https://github.com/l42111996/java-Kcp/blob/master/Benchmark.md)
> 8. [兼容kcp-go,包含fec兼容](https://github.com/l42111996/java-Kcp/blob/master/kcp-example/src/main/java/test/Kcp4GoExampleClient.java)
> 
> 
> # 兼容性:
> 1. 兼容c版本kcp
> 2. fec基于 https://github.com/Backblaze/JavaReedSolomon 实现
> 3. 完美兼容的C#版本，https://github.com/l42111996/csharp-kcp ，快速构建游戏前后端网络库
> 
> # 稳定性：
> 已经是稳定版本，据统计有5~10款上线项目接入，包含腾讯，快手等公司产品使用
> 
> # 优化:
> 1. 基于事件驱动,充分利用多核
> 2. 优化fastack逻辑，降低10%流量
> 3. 优化check函数。
> 4. 优化集合迭代器。
> 5. 包含fec,降低延迟
> 6. 附带crc32校验
> 7. 使用时间轮,优化大量连接cpu占用
> 8. 使用directbuf和对象池，无gc压力
> 9. 增加使用conv或者ip+port确定channel唯一性，游戏建议使用conv与tcp配置使用,[相关资料](https://github.com/skywind3000/kcp/wiki/Cooperate-With-Tcp-Server)
> 10. 增加游戏使用时4G切换wifi等出口ip变动不会导致连接断开
> 
> # 相关资料
> 1. https://github.com/skywind3000/kcp 原版c版本的kcp
> 2. https://github.com/xtaci/kcp-go go版本kcp,有大量优化
> 3. https://github.com/Backblaze/JavaReedSolomon java版本fec
> 4. https://github.com/LMAX-Exchange/disruptor 高性能的线程间消息传递库
> 5. https://github.com/JCTools/JCTools 高性能并发库
> 6. https://github.com/szhnet/kcp-netty java版本的一个kcp
> 7. https://github.com/l42111996/csharp-kcp 基于dotNetty的c#版本kcp,完美兼容
>    
>    
> # 交流
> 原作者QQ:526167774
   
    