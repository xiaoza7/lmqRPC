# lmqRPC
分布式RPC框架， 使用zookeeper作为注册中心

1,与spring集成，使用importBeandefinitionRegistarar引入netty 消费客户端接口消费，类似mybatis的mapper，支持注解调用，免去配置spring的xml，
2，自定义beanPostProcessor提供服务启动与注册，netty作为服务提供，与spring集成
3，对于额外的lib下的interface建议使用接口extends，然后把appconfig的ComponentScan修改

运行：
首先起本机zookeeper(port:2181),
其中myservice包是配置service,
运行test包的例子


特点：
  基于注解的服务提供方启动与注册，注解的服务消费，免去xml的配置

 不足：
    在服务消费时较慢，仍需完善


测试截图：
![Image text](https://github.com/xiaoza7/lmqRPC/blob/master/src/test/java/testservice/mb.png)

以及
![Image text](https://github.com/xiaoza7/lmqRPC/blob/master/src/test/java/testservice/ma.png)
