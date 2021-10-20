## 1.简介
>本文主要介绍SpringBoot2.3.5 + SpringCloudHoxton.SR9 + SpringCloudAlibaba 2.2.6 + OpenFeign 3.8.0 + Mybatis-Plus 3.4.2 + Nacos 1.4.2 + Seata 1.4.2 整合来实现SpringCloud分布式事务管理，使用Nacos 作为 SpringCloud和Seata的注册中心和配置中心,使用 MySQL 数据库和 MyBatis-Plus来操作数据。

如果你还对`SpringBoot`、`SpringCloudAlibaba`、`Nacos`、`Seata`、` Mybatis-Plus` 不是很了解的话，这里我为大家整理个它们的官网网站，如下

- SpringBoot：[https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)

 - SpringCloudAlibaba：[https://spring.io/projects/spring-cloud-alibaba](https://spring.io/projects/spring-cloud-alibaba)

- Nacos：[https://nacos.io/zh-cn/docs/quick-start.html](https://nacos.io/zh-cn/docs/quick-start.html)

- Seata：[https://seata.io/zh-cn/](https://seata.io/zh-cn/)

- MyBatis-Plus：[https://mp.baomidou.com/guide/](https://mp.baomidou.com/guide/)

在这里我们就不一个一个介绍它们是怎么使用和原理，详细请学习官方文档，在这里我将开始对它们进行整合，完成一个简单的案例，来让大家了解`Seata`来实现`Dubbo`分布式事务管理的基本流程。

## 2.环境准备
## 2.1 下载nacos并安装启动
nacos下载：[https://github.com/alibaba/nacos/releases/tag/1.4.2](https://github.com/alibaba/nacos/releases/tag/1.4.2)

Nacos 快速入门：[https://nacos.io/en-us/docs/quick-start.html](https://nacos.io/en-us/docs/quick-start.html)

```shell
sh startup.sh -m standalone
```

Nacos Docker 快速开始
```shell
cd doc/nacos-standalone
docker-compose up -d
```

在浏览器打开Nacos web 控制台：http://192.168.10.200:8848/nacos/index.html

输入nacos的账号和密码 分别为`nacos：nacos`

## 2.2 下载seata server 并安装启动

#### 2.2.1 在 [Seata Release](https://github.com/seata/seata/releases) 下载最新版的 Seata Server 并解压得到如下目录：


```shell
.
├──bin
├──conf
├──file_store
└──lib
```
#### 2.2.2 修改 conf/registry.conf 配置，
目前seata支持如下的file、nacos 、apollo、zk、consul的注册中心和配置中心。这里我们以`nacos` 为例。
将 type 改为 nacos

```bash
registry {
  # file nacos
  type = "nacos"

  nacos {
    serverAddr = "192.168.10.200:8848"
    namespace = ""
    cluster = "default"
  }
  file {
    name = "file.conf"
  }
}

config {
  # file、nacos 、apollo、zk、consul
  type = "nacos"

  nacos {
    serverAddr = "192.168.10.200:8848"
    namespace = ""
  }

  file {
    name = "file.conf"
  }
}
```
- serverAddr = "192.168.10.200:8848"   ：nacos 的地址
- namespace = "" ：nacos的命名空间默认为``
- cluster = "default"  ：集群设置未默认 `default`

**注意： seata0.9.0之后，配置如下, 其中`namespace = ""`**
#### 2.2.3 修改 conf/nacos-config.txt配置

```
transport.type=TCP
transport.server=NIO
transport.heartbeat=true
transport.enableClientBatchSendRequest=true
transport.threadFactory.bossThreadPrefix=NettyBoss
transport.threadFactory.workerThreadPrefix=NettyServerNIOWorker
transport.threadFactory.serverExecutorThreadPrefix=NettyServerBizHandler
transport.threadFactory.shareBossWorker=false
transport.threadFactory.clientSelectorThreadPrefix=NettyClientSelector
transport.threadFactory.clientSelectorThreadSize=1
transport.threadFactory.clientWorkerThreadPrefix=NettyClientWorkerThread
transport.threadFactory.bossThreadSize=1
transport.threadFactory.workerThreadSize=default
transport.shutdown.wait=3
service.vgroupMapping.my_test_tx_group=default
service.vgroupMapping.seata-order-service-group=default
service.vgroupMapping.seata-account-service-group=default
service.vgroupMapping.seata-storage-service-group=default
service.vgroupMapping.seata-business-service-group=default
service.default.grouplist=192.168.10.200:8091
service.enableDegrade=false
service.disableGlobalTransaction=false
client.rm.asyncCommitBufferLimit=10000
client.rm.lock.retryInterval=10
client.rm.lock.retryTimes=30
client.rm.lock.retryPolicyBranchRollbackOnConflict=true
client.rm.reportRetryCount=5
client.rm.tableMetaCheckEnable=false
client.rm.tableMetaCheckerInterval=60000
client.rm.sqlParserType=druid
client.rm.reportSuccessEnable=false
client.rm.sagaBranchRegisterEnable=false
client.rm.sagaJsonParser=fastjson
client.rm.tccActionInterceptorOrder=-2147482648
client.tm.commitRetryCount=5
client.tm.rollbackRetryCount=5
client.tm.defaultGlobalTransactionTimeout=60000
client.tm.degradeCheck=false
client.tm.degradeCheckAllowTimes=10
client.tm.degradeCheckPeriod=2000
client.tm.interceptorOrder=-2147482648
store.mode=db
store.lock.mode=file
store.session.mode=file
store.publicKey=
store.file.dir=file_store/data
store.file.maxBranchSessionSize=16384
store.file.maxGlobalSessionSize=512
store.file.fileWriteBufferCacheSize=16384
store.file.flushDiskMode=async
store.file.sessionReloadReadSize=100
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.jdbc.Driver
store.db.url=jdbc:mysql://192.168.10.200:3306/seata?useUnicode=true&rewriteBatchedStatements=true
store.db.user=root
store.db.password=123456
store.db.minConn=5
store.db.maxConn=30
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.distributedLockTable=distributed_lock
store.db.queryLimit=100
store.db.lockTable=lock_table
store.db.maxWait=5000
store.redis.mode=single
store.redis.single.host=127.0.0.1
store.redis.single.port=6379
store.redis.sentinel.masterName=
store.redis.sentinel.sentinelHosts=
store.redis.maxConn=10
store.redis.minConn=1
store.redis.maxTotal=100
store.redis.database=0
store.redis.password=
store.redis.queryLimit=100
server.recovery.committingRetryPeriod=1000
server.recovery.asynCommittingRetryPeriod=1000
server.recovery.rollbackingRetryPeriod=1000
server.recovery.timeoutRetryPeriod=1000
server.maxCommitRetryTimeout=-1
server.maxRollbackRetryTimeout=-1
server.rollbackRetryTimeoutUnlockEnable=false
server.distributedLockExpireTime=10000
client.undo.dataValidation=true
client.undo.logSerialization=jackson
client.undo.onlyCareUpdateColumns=true
server.undo.logSaveDays=7
server.undo.logDeletePeriod=86400000
client.undo.logTable=undo_log
client.undo.compress.enable=true
client.undo.compress.type=zip
client.undo.compress.threshold=64k
log.exceptionRate=100
transport.serialization=seata
transport.compressor=none
metrics.enabled=false
metrics.registryType=compact
metrics.exporterList=prometheus
metrics.exporterPrometheusPort=9898
tcc.fence.logTableName=tcc_fence_log
tcc.fence.cleanPeriod=1h
```
配置的详细说明参考官网：[https://seata.io/zh-cn/docs/user/configurations.html](https://seata.io/zh-cn/docs/user/configurations.html)

这里主要修改了如下几项：
- store.mode :存储模式 默认file  这里我修改为db 模式 ，并且需要三个表`global_table`、`branch_table`和`lock_table`
- store.db.driver-class-name： 0.8.0版本默认没有，会报错。添加了 `com.mysql.jdbc.Driver`
- store.db.datasource=dbcp ：数据源 dbcp
- store.db.db-type=mysql : 存储数据库的类型为`mysql`
- store.db.url=jdbc:mysql://192.168.10.200:3306/seata?useUnicode=true : 修改为自己的数据库`url`、`port`、`数据库名称`
- store.db.user=root :数据库的账号
- store.db.password=123456 :数据库的密码
- service.vgroupMapping.seata-order-service-group=default
- service.vgroupMapping.seata-account-service-group=default
- service.vgroupMapping.seata-storage-service-group=default
- service.vgroupMapping.seata-business-service-group=default
- client.support.spring.datasource.autoproxy=true 开启数据源自动代理 
注意最后一项可以不配置默认是开启的

也可以在 Nacos 配置页面添加，data-id 为 service.vgroup_mapping.${YOUR_SERVICE_NAME}-seata-service-group, group 为 SEATA_GROUP， 如果不添加该配置，启动后会提示no available server to connect

**注意：** 配置文件末尾有空行，需要删除，否则会提示失败，尽管实际上是成功的

***db模式下的所需的三个表的数据库脚本位于`doc\sql\db_store.sql`***

`global_table`的表结构

```sql
CREATE TABLE `global_table` (
  `xid` varchar(128) NOT NULL,
  `transaction_id` bigint(20) DEFAULT NULL,
  `status` tinyint(4) NOT NULL,
  `application_id` varchar(64) DEFAULT NULL,
  `transaction_service_group` varchar(64) DEFAULT NULL,
  `transaction_name` varchar(64) DEFAULT NULL,
  `timeout` int(11) DEFAULT NULL,
  `begin_time` bigint(20) DEFAULT NULL,
  `application_data` varchar(2000) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modified` datetime DEFAULT NULL,
  PRIMARY KEY (`xid`),
  KEY `idx_gmt_modified_status` (`gmt_modified`,`status`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

```

`branch_table`的表结构

```sql
CREATE TABLE `branch_table` (
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(128) NOT NULL,
  `transaction_id` bigint(20) DEFAULT NULL,
  `resource_group_id` varchar(32) DEFAULT NULL,
  `resource_id` varchar(256) DEFAULT NULL,
  `lock_key` varchar(128) DEFAULT NULL,
  `branch_type` varchar(8) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `application_data` varchar(2000) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modified` datetime DEFAULT NULL,
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

```

`lock_table`的表结构

```sql
create table `lock_table` (
  `row_key` varchar(128) not null,
  `xid` varchar(96),
  `transaction_id` long ,
  `branch_id` long,
  `resource_id` varchar(256) ,
  `table_name` varchar(32) ,
  `pk` varchar(32) ,
  `gmt_create` datetime ,
  `gmt_modified` datetime,
  primary key(`row_key`)
);
```

#### 2.2.4 将 Seata 配置添加到 Nacos 中
使用命令如下
```bash
cd doc/seata-standalone/script/config-center/nacos
sh nacos-config.sh -h localhost -p 8848 -g SEATA_GROUP -t 5a3c7d6c-f497-4d68-a71a-2e5e3340b3ca -u username -w password
```
参数说明：

-h：主机，默认值为localhost。

-p：端口，默认值为8848。

-g：配置分组，默认值为'SEATA_GROUP'。

-t：租户信息，对应Nacos的namespace ID字段，默认值为''。

-u：用户名，nacos 1.2.0+ 权限控制，默认为''。

-w：密码，nacos 1.2.0+ 关于权限控制，默认值为''

python:
```bash
cd doc/seata-standalone/script/config-center/nacos
python nacos-config.py localhost:8848
```

成功后会提示

```
init nacos config finished, please start seata-server
```

在 Nacos 管理页面应该可以看到有 98 个 Group 为SEATA_GROUP的配置 

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019090510533734.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9saWRvbmcxNjY1LmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

#### 2.2.5 启动 Seata Server

使用db 模式启动

```shell
 cd ..
 sh ./bin/seata-server.sh
```
#### 2.2.6 Docker Compose 启动 Seata Server

使用db + 自定义registry.conf 模式启动

```shell
 cd doc/seata-standalone
 docker-compose up -d
```

这时候在 Nacos 的服务列表下面可以看到一个名为serverAddr的服务
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905110455278.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9saWRvbmcxNjY1LmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)



## 3 使用seata-spring-boot-starter案例分析
`seata-spring-boot-starter`是使用springboot自动装配来简化seata-all的复杂配置。1.0.0可用于替换seata-all，`GlobalTransactionScanner`自动初始化（依赖SpringUtils）若其他途径实现`GlobalTransactionScanner`初始化，请保证`io.seata.spring.boot.autoconfigure.util.SpringUtils`先初始化；
`seata-spring-boot-starter`默认开启数据源自动代理，用户若再手动配置`DataSourceProxy`将会导致异常。


参考官网中用户购买商品的业务逻辑。整个业务逻辑由4个微服务提供支持：

- 库存服务：扣除给定商品的存储数量。
- 订单服务：根据购买请求创建订单。
- 帐户服务：借记用户帐户的余额。
- 业务服务：处理业务逻辑。

请求逻辑架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905111031350.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9saWRvbmcxNjY1LmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

## 3.1  github地址
seata-alibaba-demo：[https://github.com/solvaysphere/seata-alibaba-demo](https://github.com/solvaysphere/seata-alibaba-demo)
- seata-common-service ：公共模块

- seata-account-service ：用户账号模块

- seata-order-service ：订单模块

- seata-storage-service ：库存模块

- seata-business-service ：业务模块


#### 3.2 账户服务：AccountDubboService

```java
/**
 * @Description  账户服务接口
 */
public interface AccountDubboService {

    /**
     * 从账户扣钱
     */
    ObjectResponse decreaseAccount(AccountDTO accountDTO);
}
```
#### 3.3 订单服务：OrderDubboService

```java
/**
 * @Description  订单服务接口
 */
public interface OrderDubboService {

    /**
     * 创建订单
     */
    R<OrderDTO> createOrder(OrderDTO orderDTO);
}
```
#### 3.4  库存服务：StorageDubboService

```java
/**
 * @Description  库存服务
 */
public interface StorageDubboService {

    /**
     * 扣减库存
     */
    boolean decreaseStorage(CommodityDTO commodityDTO);
}

```

#### 3.5 业务服务：BusinessService 

```java

/**
 * @Description
 */
public interface BusinessService {

    /**
     * 出处理业务服务
      * @param businessDTO
     * @return
     */
    R handleBusiness(BusinessDTO businessDTO);
}
```

业务逻辑的具体实现主要体现在 订单服务的实现和业务服务的实现

订单服务的实现
```java
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private AccountFeignClient accountFeignClient;

    /**
     * 创建订单
     * @Param:  OrderDTO  订单对象
     * @Return:  OrderDTO  订单对象
     */
    @Override
    public R<OrderDTO> createOrder(OrderDTO orderDTO) {
        //扣减用户账户
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(orderDTO.getUserId());
        accountDTO.setAmount(orderDTO.getOrderAmount());
        R accountResponse = accountFeignClient.decreaseAccount(accountDTO);
        //生成订单号
        orderDTO.setOrderNo(UUID.randomUUID().toString().replace("-",""));
        //生成订单
        Order order = new Order();
        BeanUtils.copyProperties(orderDTO,order);
        order.setCount(orderDTO.getOrderCount());
        order.setAmount(orderDTO.getOrderAmount().doubleValue());
        try {
            baseMapper.createOrder(order);
        } catch (Exception e) {
            return R.failed(ApiErrorCode.FAILED);
        }

        if (accountResponse.getCode() != ApiErrorCode.SUCCESS.getCode()) {
            return R.failed(ApiErrorCode.FAILED);
        }
        return R.ok(orderDTO);
    }
}
```

整个业务的实现逻辑
```java
@Service("businessService")
@Slf4j
public class BusinessServiceImpl implements BusinessService{

    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private StorageFeignClient storageFeignClient;

    boolean flag;

    /**
     * 处理业务逻辑 正常的业务逻辑
     * @param businessDTO
     * @return
     */
    @GlobalTransactional(timeoutMills = 300000, name = "feign-gts-seata-demo")
    @Override
    public R handleBusiness(BusinessDTO businessDTO) {
        log.info("开始全局事务，XID = " + RootContext.getXID());
        //1、扣减库存
        CommodityDTO commodityDTO = new CommodityDTO();
        commodityDTO.setCommodityCode(businessDTO.getCommodityCode());
        commodityDTO.setCount(businessDTO.getCount());
        R storageResponse = storageFeignClient.decreaseStorage(commodityDTO);
        //2、创建订单
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(businessDTO.getUserId());
        orderDTO.setCommodityCode(businessDTO.getCommodityCode());
        orderDTO.setOrderCount(businessDTO.getCount());
        orderDTO.setOrderAmount(businessDTO.getAmount());
        R<OrderDTO> response = orderFeignClient.createOrder(orderDTO);

        if (storageResponse.getCode() != ApiErrorCode.SUCCESS.getCode() || response.getCode() != ApiErrorCode.SUCCESS.getCode()) {
            throw new ApiException(ApiErrorCode.FAILED);
        }
        return R.ok(response.getData());
    }

    /**
     * 处理业务服务，出现异常回顾
     * @param businessDTO
     * @return
     */
    @GlobalTransactional(timeoutMills = 300000, name = "feign-gts-seata-demo")
    @Override
    public R handleBusiness2(BusinessDTO businessDTO) {
        log.info("开始全局事务，XID = " + RootContext.getXID());
        //1、扣减库存
        CommodityDTO commodityDTO = new CommodityDTO();
        commodityDTO.setCommodityCode(businessDTO.getCommodityCode());
        commodityDTO.setCount(businessDTO.getCount());
        R storageResponse = storageFeignClient.decreaseStorage(commodityDTO);
        //2、创建订单
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(businessDTO.getUserId());
        orderDTO.setCommodityCode(businessDTO.getCommodityCode());
        orderDTO.setOrderCount(businessDTO.getCount());
        orderDTO.setOrderAmount(businessDTO.getAmount());
        R<OrderDTO> response = orderFeignClient.createOrder(orderDTO);

//        打开注释测试事务发生异常后，全局回滚功能
        if (!flag) {
            throw new RuntimeException("测试抛异常后，分布式事务回滚！");
        }

        if (storageResponse.getCode() != ApiErrorCode.SUCCESS.getCode() || response.getCode() != ApiErrorCode.SUCCESS.getCode()) {
            throw new ApiException(ApiErrorCode.FAILED);
        }
        return R.ok(response.getData());
    }
}
```
## 3.6 使用seata的分布式事务解决方案处理SpringCloud的分布式事务
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905113350848.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9saWRvbmcxNjY1LmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

我们只需要在业务处理的方法`handleBusiness`添加一个注解 `@GlobalTransactional` 

```java
@Service("businessService")
@Slf4j
public class BusinessServiceImpl implements BusinessService{
    @GlobalTransactional(timeoutMills = 300000, name = "dubbo-gts-seata-example")
    @Override
    public R handleBusiness(BusinessDTO businessDTO) {
        // TODO
        return null;
    }
}
```
- `timeoutMills`: 超时时间
- `name ` ：事务名称

## 3.7 准备数据库
注意: MySQL必须使用`InnoDB engine`.

创建数据库  并导入数据库脚本
```sql
DROP DATABASE IF EXISTS seata;
CREATE DATABASE seata;
USE seata;

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

-- ----------------------------
-- Records of undo_log
-- ----------------------------
SET FOREIGN_KEY_CHECKS=1;
```

会看到如上的4个表

```shell
+-------------------------+
| Tables_in_seata         |
+-------------------------+
| t_account               |
| t_order                 |
| t_storage               |
| undo_log                |
+-------------------------+
```

这里为了简化我将这个三张表创建到一个库中,使用是三个数据源来实现。

## 3.8 我们以账号服务`seata-account-service`为例 ，分析需要注意的配置项目
### 3.8.1 引入的依赖
核心Pom文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.5.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <artifactId>seata-alibaba-demo</artifactId>
    <packaging>pom</packaging>
    <name>seata-alibaba-demo</name>
    <groupId>com.solvay</groupId>
    <version>0.0.1-SNAPSHOT</version>
    <description>Demo project for Spring Cloud Alibaba Seata</description>

    <modules>
        <module>seata-common-service</module>
        <module>seata-storage-service</module>
        <module>seata-order-service</module>
        <module>seata-account-service</module>
        <module>seata-business-service</module>
    </modules>

    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <spring-boot.version>2.3.5.RELEASE</spring-boot.version>
        <spring-cloud-alibaba.version>2.2.6.RELEASE</spring-cloud-alibaba.version>
        <spring-cloud.version>Hoxton.SR9</spring-cloud.version>
        <mybatis-plus.version>3.4.2</mybatis-plus.version>
        <seata.version>1.4.2</seata.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>io.seata</groupId>
                    <artifactId>seata-spring-boot-starter</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-spring-boot-starter</artifactId>
            <version>${seata.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!--druid-->
        <!--<dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.2.4</version>
        </dependency>-->

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.solvay</groupId>
                <artifactId>seata-common-service</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

```
其中seata-account-service的Pom文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.solvay</groupId>
        <artifactId>seata-alibaba-demo</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>seata-account-service</artifactId>
    <name>seata-account-service</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.solvay</groupId>
            <artifactId>seata-common-service</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

注意：
- `seata-spring-boot-starter`: 这个是spring-boot seata 所需的主要依赖，1.4.2版本开始加入支持。

其他的就不一一介绍，其他的一目了然，就知道是干什么的。
  
 ### 3.8.2  application.properties配置
 

```properties
# 应用端口号
server.port=8102
# 应用名称
spring.application.name=seata-account-service
# 数据库驱动：
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# 数据源名称
spring.datasource.name=seata
# 数据库连接地址
spring.datasource.url=jdbc:mysql://192.168.10.200:3306/seata?serverTimezone=UTC
# 数据库用户名&密码：
spring.datasource.username=root
spring.datasource.password=123456
# Nacos帮助文档: https://nacos.io/zh-cn/docs/concepts.html
# Nacos认证信息
#spring.cloud.nacos.discovery.username=nacos
#spring.cloud.nacos.discovery.password=nacos
# Nacos 服务发现与注册配置，其中子属性 server-addr 指定 Nacos 服务器主机和端口
spring.cloud.nacos.discovery.server-addr=192.168.10.200:8848
# 注册到 nacos 的指定 namespace，默认为 public
spring.cloud.nacos.discovery.namespace=public

logging.level.com.solvay.storage=debug

# Mybytis Plus Config
mybatis-plus.mapper-locations=classpath*:/mapper/*.xml
mybatis-plus.type-aliases-package=com.solvay.account.entity
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.global-config.db-config.id-type=auto

# Seata Config
seata.enabled=true
seata.application-id=${spring.application.name}
# 事务群组（可以每个应用独立取名，也可以使用相同的名字）
seata.tx-service-group=${spring.application.name}-group
# TC 集群（必须与seata-server保持一致）
seata.service.vgroup-mapping.${spring.application.name}-group=default
seata.service.grouplist.default=134.134.2.78:8091

seata.registry.type=nacos
#seata.registry.nacos.application=seata-server
seata.registry.nacos.server-addr=192.168.10.200:8848
seata.registry.nacos.group=SEATA_GROUP
seata.registry.nacos.cluster=default
seata.registry.nacos.namespace=
seata.registry.nacos.username=
seata.registry.nacos.password=

seata.config.type=nacos
seata.config.nacos.serverAddr=192.168.10.200:8848
seata.config.nacos.namespace=
seata.config.nacos.group=SEATA_GROUP
seata.config.nacos.username=
seata.config.nacos.password=
```
### 3.8.3 SeataDataSourceAutoConfig  配置 此项配置可以不用配，Seata默认是支持数据源自动代理

```java
package io.seata.samples.integration.account.config;

import com.alibaba.druid.pool.DruidDataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @Author: lidong
 * @Description  seata global configuration
 * @Date Created in 2019/9/05 10:28
 */
@Configuration
public class SeataDataSourceAutoConfig {

    /**
     * autowired datasource config
     */
    @Autowired
    private DataSourceProperties dataSourceProperties;

    /**
     * init durid datasource
     *
     * @Return: druidDataSource  datasource instance
     */
    @Bean
    @Primary
    public DruidDataSource druidDataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dataSourceProperties.getUrl());
        druidDataSource.setUsername(dataSourceProperties.getUsername());
        druidDataSource.setPassword(dataSourceProperties.getPassword());
        druidDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        druidDataSource.setInitialSize(0);
        druidDataSource.setMaxActive(180);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setMinIdle(0);
        druidDataSource.setValidationQuery("Select 1 from DUAL");
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(25200000);
        druidDataSource.setRemoveAbandoned(true);
        druidDataSource.setRemoveAbandonedTimeout(1800);
        druidDataSource.setLogAbandoned(true);
        return druidDataSource;
    }
    /**
     * init mybatis sqlSessionFactory
     * @Param: dataSourceProxy  datasource proxy
     * @Return: DataSourceProxy  datasource proxy
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:/mapper/*.xml"));
        return factoryBean.getObject();
    }
}

```
### 3.8.4 SeataAccountServiceApplication 启动类的配置

```java
package com.solvay.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.solvay.account.mapper")
public class SeataAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataAccountServiceApplication.class, args);
    }

}


```

## 4 启动所有的service模块
启动 `seata-account-service`、`seata-order-service`、`seata-storage-service`、`seata-business-service`

并且在nocos的控制台查看注册情况: http://192.168.10.200:8848/nacos/#/serviceManagement

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905131449502.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9saWRvbmcxNjY1LmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

我们可以看到上面的服务都已经注册成功。

## 5 测试
### 5. 1 发送一个下单请求
使用postman 发送 ：[http://localhost:8104/business/buy](http://localhost:8104/business/buy) 

参数：

```json
{
    "userId": "1",
    "amount": 100,
    "count": 10,
    "name": "水杯",
    "commodityCode": "C201901140001"
}
```
返回

```json
{
    "code": 0,
    "data": {
        "orderNo": "64e0d9dec78b4fb9a9e74479a472c9ab",
        "userId": "1",
        "commodityCode": "C201901140001",
        "orderCount": 50,
        "orderAmount": 100
    },
    "msg": "执行成功"
}
```
这时候控制台：

```
2021-10-15 16:52:08.347  INFO 57595 --- [nio-8104-exec-7] c.s.b.controller.BusinessController      : 请求参数：BusinessDTO(userId=1, commodityCode=C201901140001, name=水杯, count=50, amount=100)
2021-10-15 16:52:08.437  INFO 57595 --- [nio-8104-exec-7] i.seata.tm.api.DefaultGlobalTransaction  : Begin new global transaction [192.168.10.200:8091:18202172631351361]
2021-10-15 16:52:08.441  INFO 57595 --- [nio-8104-exec-7] c.s.b.service.impl.BusinessServiceImpl   : 开始全局事务，XID = 192.168.10.200:8091:18202172631351361
2021-10-15 16:52:14.551  INFO 57595 --- [nio-8104-exec-7] i.seata.tm.api.DefaultGlobalTransaction  : Suspending current transaction, xid = 192.168.10.200:8091:18202172631351361
2021-10-15 16:52:14.554  INFO 57595 --- [nio-8104-exec-7] i.seata.tm.api.DefaultGlobalTransaction  : [192.168.10.103:8091:18202172631351361] commit status: Committed
```
事务提交成功，

我们来看一下数据库数据变化

t_account 
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905122211274.png)
t_order

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905122302472.png)
t_storage

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190905122326182.png)
数据没有问题。

### 5.2 测试回滚
我们`seata-business-service`将`BusinessServiceImpl`的`handleBusiness2` 下面的代码去掉注释

```
if (!flag) {
  throw new RuntimeException("测试抛异常后，分布式事务回滚！");
}
```
使用postman 发送 ：[http://localhost:8104/business/buy2](http://localhost:8104/business/dubbo/buy2) 

.响应结果：

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
#### 5.2.1 business控制台日志

```
2021-10-15 17:10:41.224  INFO 57595 --- [nio-8104-exec-9] c.s.b.controller.BusinessController      : 请求参数：BusinessDTO(userId=1, commodityCode=C201901140001, name=水杯, count=50, amount=100)
2021-10-15 17:10:41.264  INFO 57595 --- [nio-8104-exec-9] i.seata.tm.api.DefaultGlobalTransaction  : Begin new global transaction [192.168.10.200:8091:18202172631351377]
2021-10-15 17:10:41.267  INFO 57595 --- [nio-8104-exec-9] c.s.b.service.impl.BusinessServiceImpl   : 开始全局事务，XID = 192.168.10.200:8091:18202172631351377
2021-10-15 17:10:45.078  INFO 57595 --- [nio-8104-exec-9] i.seata.tm.api.DefaultGlobalTransaction  : Suspending current transaction, xid = 192.168.10.200:8091:18202172631351377
2021-10-15 17:10:45.083  INFO 57595 --- [nio-8104-exec-9] i.seata.tm.api.DefaultGlobalTransaction  : [192.168.10.200:8091:18202172631351377] rollback status: Rollbacked
2021-10-15 17:10:45.404 ERROR 57595 --- [nio-8104-exec-9] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.RuntimeException: 测试抛异常后，分布式事务回滚！] with root cause

java.lang.RuntimeException: 测试抛异常后，分布式事务回滚！
	at com.solvay.business.service.impl.BusinessServiceImpl.handleBusiness2(BusinessServiceImpl.java:81) ~[classes/:na]
	at com.solvay.business.service.impl.BusinessServiceImpl$$FastClassBySpringCGLIB$$a4645ab.invoke(<generated>) ~[classes/:na]
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218) ~[spring-core-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:771) ~[spring-aop-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749) ~[spring-aop-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at io.seata.spring.annotation.GlobalTransactionalInterceptor$2.execute(GlobalTransactionalInterceptor.java:184) ~[seata-all-1.4.2.jar:1.4.2]
	at io.seata.tm.api.TransactionalTemplate.execute(TransactionalTemplate.java:127) ~[seata-all-1.4.2.jar:1.4.2]
	at io.seata.spring.annotation.GlobalTransactionalInterceptor.handleGlobalTransaction(GlobalTransactionalInterceptor.java:181) ~[seata-all-1.4.2.jar:1.4.2]
	at io.seata.spring.annotation.GlobalTransactionalInterceptor.invoke(GlobalTransactionalInterceptor.java:150) ~[seata-all-1.4.2.jar:1.4.2]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749) ~[spring-aop-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:691) ~[spring-aop-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at com.solvay.business.service.impl.BusinessServiceImpl$$EnhancerBySpringCGLIB$$599b240f.handleBusiness2(<generated>) ~[classes/:na]
	at com.solvay.business.controller.BusinessController.handleBusiness2(BusinessController.java:40) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_211]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_211]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_211]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_211]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:105) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:878) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:792) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1040) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:943) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:652) ~[tomcat-embed-core-9.0.39.jar:4.0.FR]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883) ~[spring-webmvc-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:733) ~[tomcat-embed-core-9.0.39.jar:4.0.FR]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[spring-web-5.2.10.RELEASE.jar:5.2.10.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202) ~[tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:374) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:868) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1590) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149) [na:1.8.0_211]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624) [na:1.8.0_211]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) [tomcat-embed-core-9.0.39.jar:9.0.39]
	at java.lang.Thread.run(Thread.java:748) [na:1.8.0_211]
```


#### 5.2.2 account服务控制台日志

```bash
2021-10-15 17:10:43.672  INFO 57574 --- [nio-8102-exec-7] c.s.a.controller.AccountController       : 请求账户微服务：AccountDTO(id=null, userId=1, amount=100)
2021-10-15 17:10:43.877  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@6e10e8d5 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.889  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@64e883a5 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.909  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@21ea015c (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.916  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@48d513f4 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.940  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@1081a5ba (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.958  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@3054e9a4 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.967  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@41c5fe13 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.977  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@216f1f6c (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.982  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@3c5ec28e (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:43.992  WARN 57574 --- [nio-8102-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@2b500568 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.085 DEBUG 57574 --- [nio-8102-exec-7] c.s.a.m.AccountMapper.decreaseAccount    : ==>  Preparing: update t_account set amount = amount-100.0 where user_id = ?
2021-10-15 17:10:44.095 DEBUG 57574 --- [nio-8102-exec-7] c.s.a.m.AccountMapper.decreaseAccount    : ==> Parameters: 1(String)
2021-10-15 17:10:44.232 DEBUG 57574 --- [nio-8102-exec-7] c.s.a.m.AccountMapper.decreaseAccount    : <==    Updates: 1
2021-10-15 17:10:44.835  INFO 57574 --- [h_RMROLE_1_4_16] i.s.c.r.p.c.RmBranchRollbackProcessor    : rm handle branch rollback process:xid=192.168.10.200:8091:18202172631351377,branchId=18202172631351384,branchType=AT,resourceId=jdbc:mysql://192.168.10.200:3306/seata,applicationData=null
2021-10-15 17:10:44.838  INFO 57574 --- [h_RMROLE_1_4_16] io.seata.rm.AbstractRMHandler            : Branch Rollbacking: 192.168.10.200:8091:18202172631351377 18202172631351384 jdbc:mysql://192.168.10.200:3306/seata
2021-10-15 17:10:44.894  INFO 57574 --- [h_RMROLE_1_4_16] i.s.r.d.undo.AbstractUndoLogManager      : xid 192.168.10.200:8091:18202172631351377 branch 18202172631351384, undo_log deleted with GlobalFinished
2021-10-15 17:10:44.895  INFO 57574 --- [h_RMROLE_1_4_16] io.seata.rm.AbstractRMHandler            : Branch Rollbacked result: PhaseTwo_Rollbacked
```
#### 5.2.3 order服务控制台日志

```bash
2021-10-15 17:10:42.437  INFO 57602 --- [nio-8101-exec-7] c.s.order.controller.OrderController     : 请求订单微服务：OrderDTO(orderNo=null, userId=1, commodityCode=C201901140001, orderCount=10, orderAmount=100)
2021-10-15 17:10:44.378  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@161da6e9 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.424  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@26e97e5 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.440  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@76c81097 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.484  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@d19ca49 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.497  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@4dff0ff2 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.539  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@4e2a433c (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.545  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@7b9f4594 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.548  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@37f0230 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.558  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@1a9e798f (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.564  WARN 57602 --- [nio-8101-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@631fa43 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:44.603 DEBUG 57602 --- [nio-8101-exec-7] c.s.o.mapper.OrderMapper.createOrder     : ==>  Preparing: insert into t_order values(null,?,?,?,10,100.0)
2021-10-15 17:10:44.609 DEBUG 57602 --- [nio-8101-exec-7] c.s.o.mapper.OrderMapper.createOrder     : ==> Parameters: 7d75e8bb2a984494b5d9c648a5cd4c2b(String), 1(String), C201901140001(String)
2021-10-15 17:10:44.692 DEBUG 57602 --- [nio-8101-exec-7] c.s.o.mapper.OrderMapper.createOrder     : <==    Updates: 1
2021-10-15 17:10:44.751  INFO 57602 --- [h_RMROLE_1_4_16] i.s.c.r.p.c.RmBranchRollbackProcessor    : rm handle branch rollback process:xid=192.168.10.200:8091:18202172631351377,branchId=18202172631351386,branchType=AT,resourceId=jdbc:mysql://192.168.10.200:3306/seata,applicationData=null
2021-10-15 17:10:44.751  INFO 57602 --- [h_RMROLE_1_4_16] io.seata.rm.AbstractRMHandler            : Branch Rollbacking: 192.168.10.200:8091:18202172631351377 18202172631351386 jdbc:mysql://192.168.10.200:3306/seata
2021-10-15 17:10:44.787  INFO 57602 --- [h_RMROLE_1_4_16] i.s.r.d.undo.AbstractUndoLogManager      : xid 192.168.10.200:8091:18202172631351377 branch 18202172631351386, undo_log deleted with GlobalFinished
2021-10-15 17:10:44.789  INFO 57602 --- [h_RMROLE_1_4_16] io.seata.rm.AbstractRMHandler            : Branch Rollbacked result: PhaseTwo_Rollbacked
```

#### 5.2.4 storage服务控制台日志

```bash
2021-10-15 17:10:41.558  INFO 57588 --- [nio-8109-exec-7] c.s.s.controller.StorageController       : 请求库存微服务：CommodityDTO(id=null, commodityCode=C201901140001, name=null, count=10)
2021-10-15 17:10:41.681  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@6ae7a9b5 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.710  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@601fc2c5 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.727  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@251d40a2 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.739  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@7260efb5 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.754  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@147207e2 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.806  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@30dafdb9 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.824  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@3493a157 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.847  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@11e11e68 (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.872  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@417c066e (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.881  WARN 57588 --- [nio-8109-exec-7] com.zaxxer.hikari.pool.PoolBase          : seata - Failed to validate connection com.mysql.cj.jdbc.ConnectionImpl@26ac0ccd (No operations allowed after connection closed.). Possibly consider using a shorter maxLifetime value.
2021-10-15 17:10:41.959 DEBUG 57588 --- [nio-8109-exec-7] c.s.s.m.StorageMapper.decreaseStorage    : ==>  Preparing: update t_storage set count = count-10 where commodity_code = ?
2021-10-15 17:10:41.977 DEBUG 57588 --- [nio-8109-exec-7] c.s.s.m.StorageMapper.decreaseStorage    : ==> Parameters: C201901140001(String)
2021-10-15 17:10:42.097 DEBUG 57588 --- [nio-8109-exec-7] c.s.s.m.StorageMapper.decreaseStorage    : <==    Updates: 1
2021-10-15 17:10:44.929  INFO 57588 --- [h_RMROLE_1_4_16] i.s.c.r.p.c.RmBranchRollbackProcessor    : rm handle branch rollback process:xid=192.168.10.200:8091:18202172631351377,branchId=18202172631351380,branchType=AT,resourceId=jdbc:mysql://192.168.10.200:3306/seata,applicationData=null
2021-10-15 17:10:44.931  INFO 57588 --- [h_RMROLE_1_4_16] io.seata.rm.AbstractRMHandler            : Branch Rollbacking: 192.168.10.200:8091:18202172631351377 18202172631351380 jdbc:mysql://192.168.10.200:3306/seata
2021-10-15 17:10:45.048  INFO 57588 --- [h_RMROLE_1_4_16] i.s.r.d.undo.AbstractUndoLogManager      : xid 192.168.10.200:8091:18202172631351377 branch 18202172631351380, undo_log deleted with GlobalFinished
2021-10-15 17:10:45.049  INFO 57588 --- [h_RMROLE_1_4_16] io.seata.rm.AbstractRMHandler            : Branch Rollbacked result: PhaseTwo_Rollbacked
```

我们查看数据库数据，已经回滚，和上面的数据一致。

到这里一个简单的案例基本就分析结束。感谢你的学习。

[Seata 高可用部署实践](./README_SEATA_HA.md)
