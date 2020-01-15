
因为再在rpc里面继续深入和学习netty关系不大了，有点向工程上面跑的感觉，所以接下来主要对netty的源码进行深入的解析，希望对底层的认知更深一层，主要从以下几个方面进行钻研

1. netty的整体架构与分层
1.1 整体逻辑架构
1.2 代码分层
2. netty server的启动
3. netty server的请求处理与并发请求的处理流程
4. netty server的其它功能
5. netty client的启动
6. netty client的请求处理
7. netty client的相应
8. netty 的 零内存拷贝原理(零拷贝其实是指CPU上下文切换的次数是零，而非拷贝次数是0。)
9. netty 的 通道
10. netty 的 通道周边功能
11. netty 的 pipeline
12. netty 的 内存管理