# Spring Cloud 使用 Seata 实现分布式事务，Nacos 作为 Seata 配置中心

> 使用 Seata 作为分布式事务组件，配置中心和注册中心使用 Nacos，使用 MySQL 数据库和 MyBatis，同时使用 Nacos 作为 Seata 的配置中心

## 环境准备

### 创建数据库及表

- 业务表

```sql
DROP DATABASE IF EXISTS seata;
CREATE DATABASE seata;
USE seata;

-- ----------------------------
-- Table structure for t_account
-- ----------------------------
DROP TABLE IF EXISTS `t_account`;
CREATE TABLE `t_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `amount` double(14,2) DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_account
-- ----------------------------
INSERT INTO `t_account` VALUES ('1', '1', '4000.00');

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT '0',
  `amount` double(14,2) DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_order
-- ----------------------------

-- ----------------------------
-- Table structure for t_storage
-- ----------------------------
DROP TABLE IF EXISTS `t_storage`;
CREATE TABLE `t_storage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `commodity_code` (`commodity_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_storage
-- ----------------------------
INSERT INTO `t_storage` VALUES ('1', 'C201901140001', '水杯', '1000');
```

- Seata Undo 表

```sql
-- ----------------------------
-- Table structure for undo_log
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```

### 启动 Nacos

```bash
docker run --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server
```

### 启动 Seata Server 

1. 在 [Seata Release](https://github.com/seata/seata/releases) 下载相应版本的 Seata Server 并解压
2. 修改 `conf/registry.conf` 配置，将 type 改为 `nacos`

```
registry {
  type = "nacos"

  nacos {
    serverAddr = "localhost"
    namespace = ""
    cluster = "default"
  }
}

config {
  type = "nacos"

  nacos {
    serverAddr = "localhost"
    namespace = ""
    cluster = "default"
  }
}
```

4. 将 Seata 配置添加到 Nacos 中 

```bash
cd conf
sh nacos-config.sh localhost
```

成功后会提示

```bash
init nacos config finished, please start seata-server
```

5. 启动 Seata Server 

```bash
cd ..
sh ./bin/seata-server.sh
```

启动后在 Nacos 的服务列表下面可以看到一个名为`seata-server`的服务

## 测试


- 添加配置

在 public 的命名空间下，分别创建 data-id 为 `order-service.properties`, `pay-service.properties`, `storage-service.properties` 的配置，内容相同，需要修改数据库的地址、用户名和密码

```properties
# 数据库驱动：
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# 数据源名称
spring.datasource.name=seata
# 数据库连接地址
spring.datasource.url=jdbc:mysql://134.134.2.70:3306/seata?serverTimezone=UTC
# 数据库用户名&密码：
spring.datasource.username=root
spring.datasource.password=123456

# Seata Config
seata.enabled=true
seata.application-id=${spring.application.name}
# 事务群组（可以每个应用独立取名，也可以使用相同的名字）
seata.tx-service-group=${spring.application.name}-group
# TC 集群（必须与seata-server保持一致）
seata.service.vgroup-mapping.${spring.application.name}-group=default
seata.registry.type=nacos
#seata.registry.nacos.application=seata-server
seata.registry.nacos.server-addr=${NACOS_ADDR:localhost:8848}
seata.registry.nacos.group=SEATA_GROUP
seata.registry.nacos.cluster=default
seata.registry.nacos.namespace=d81c1660-b7ff-4b6d-b92e-d27066982ce7
seata.registry.nacos.username=nacos
seata.registry.nacos.password=nacos
seata.config.type=nacos
seata.config.nacos.serverAddr=${NACOS_ADDR:localhost:8848}
seata.config.nacos.namespace=d81c1660-b7ff-4b6d-b92e-d27066982ce7
seata.config.nacos.group=SEATA_GROUP
seata.config.nacos.username=nacos
seata.config.nacos.password=nacos
```

- 启动应用

分别启动 seata-business-service, seata-order-service，seata-account-service 和 seata-storage-service

- 测试成功场景

调用 placeOrder 接口，将 price 设置为 1，此时余额为 10，可以下单成功

```bash
curl -X POST \
  http://localhost:8104/business/buy \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "1",
    "amount": 100,
    "count": 10,
    "name": "水杯",
    "commodityCode": "C201901140001"
}'
```

此时返回结果为：

```json
{
    "code": 0,
    "data": null,
    "msg": "执行成功"
}
```

- 测试失败场景

下单失败，business-service会抛出异常，事务会回滚

```bash
curl -X POST \
  http://localhost:8104/business/buy2 \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "1",
    "amount": 100,
    "count": 10,
    "name": "水杯",
    "commodityCode": "C201901140001"
}'
```
此时返回结果为：

```json
{
    "timestamp": "2021-10-15T09:10:45.444+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "trace": "java.lang.RuntimeException: 测试抛异常后，分布式事务回滚！\n\tat com.solvay.business.service.impl.BusinessServiceImpl.handleBusiness2(BusinessServiceImpl.java:81)\n\tat com.solvay.business.service.impl.BusinessServiceImpl$$FastClassBySpringCGLIB$$a4645ab.invoke(<generated>)\n\tat org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)\n\tat org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:771)\n\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)\n\tat org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749)\n\tat io.seata.spring.annotation.GlobalTransactionalInterceptor$2.execute(GlobalTransactionalInterceptor.java:184)\n\tat io.seata.tm.api.TransactionalTemplate.execute(TransactionalTemplate.java:127)\n\tat io.seata.spring.annotation.GlobalTransactionalInterceptor.handleGlobalTransaction(GlobalTransactionalInterceptor.java:181)\n\tat io.seata.spring.annotation.GlobalTransactionalInterceptor.invoke(GlobalTransactionalInterceptor.java:150)\n\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)\n\tat org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749)\n\tat org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:691)\n\tat com.solvay.business.service.impl.BusinessServiceImpl$$EnhancerBySpringCGLIB$$599b240f.handleBusiness2(<generated>)\n\tat com.solvay.business.controller.BusinessController.handleBusiness2(BusinessController.java:40)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n\tat java.lang.reflect.Method.invoke(Method.java:498)\n\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190)\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138)\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:105)\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:878)\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:792)\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1040)\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:943)\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)\n\tat org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:652)\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:733)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:374)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:868)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1590)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\n\tat java.lang.Thread.run(Thread.java:748)\n",
    "message": "测试抛异常后，分布式事务回滚！",
    "path": "/business/buy2"
}
```
查看 undo_log 的日志或者主键，可以看到在执行过程中有保存数据

如查看主键自增的值，在执行前后的值会发生变化，在执行前是 1，执行后是 5

```sql
SELECT
    auto_increment
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'seata'
  AND TABLE_NAME = 'undo_log'
```

### 版本

需要注意的是当前Spring Boot 和 Spring Cloud 以及 Spring Cloud Alibaba 的版本号需要互相对应，否则可能会存在各种问题；具体可以参考[版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)
