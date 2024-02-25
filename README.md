# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.1/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.2.1/maven-plugin/reference/html/#build-image)

### 项目环境

* JDK 17
* spring-boot 3.2.1
* netty
* 多线程

### 项目说明

* 测试数据使用protobuf编码压缩
* 使用netty客户端连接到netty服务端，并使用PB编解码


### 配置说明
* 下载好项目后配置jdk17环境
* 配置后先执行clean，然后执行maven Plugins中的 protoc-jar:run生成pb文件
* 配置src/main/generated-sources目录为Generated Sources Root

### 业务文档
* 见服务端项目文档

### 业务流程技术说明
* 系统启动时，加载配置信息[application.yml](src%2Fmain%2Fresources%2Fapplication.yml)
* 系统启动后在MqttConnection[TcpClientBootstrap.java](src%2Fmain%2Fjava%2Fnet%2Fitfeng%2Fnettytcpdemoclient%2Fclient%2FTcpClientBootstrap.java)中创建netty连接, 并在连接成功后发送一个上线事件，然后开始业务心跳
* 当收到服务端的时间校准数据后，立即响应一条时间校准响应数据
* 当收到测试数据后，将向服务端响应一条测试数据响应