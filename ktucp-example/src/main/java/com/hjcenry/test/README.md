## 测试代码

- 使用原作者Java-KCP框架和基于Netty的TCP进行的KCP和TCP对比性能测试
- 使用protobuf定义简单的通讯协议

## 使用方式

- TestServerEnum中定义三种环境的ip和端口
- TestTcpServer和TestKcpServer分别达成jar部署，java -jar加端口号运行即可（有一个稳定的服务器环境即可）
    - 如：java -jar TestTcp.jar 8888
- 修改NetTest的main方法，修改你想测试的环境和对比结果
- 等待运行结束，结束后会输出测试结果，并已组织成python代码格式
- 粘贴运行结果到python代码（python/net-test-drawer.py）
- 运行python/net-test-drawer.py即可得到测试结果

## 例子
https://hjcenry.com/archives/java%E4%B8%AD%E4%BD%BF%E7%94%A8kcp%E5%8D%8F%E8%AE%AE%E6%80%A7%E8%83%BD%E6%B5%8B%E8%AF%95%E5%8F%8A%E5%BA%94%E7%94%A8