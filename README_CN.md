# DoPaaS
![DoPaaS](shots/logo.jpg)

[中文文档](README_CN.md) | English version goes [here](README.md)

<b style="color:red">注：当前完全分布式版正在孵化中，不建议在生产环境使用！</b>

## 一站式基于DevSecOps的PaaS平台解决方案
> 基于SpringCloud/Docker/ServiceMesh(istio)，主要模块：统一资产管理中心(CMDB)、统一持续交付中心(分布式编译CI/CD)、IAM认证中心(rbac/oauth2/oidc/[saml2])、统一监控中心(sba/zipkin/promethous)、统一配置中心、统一作业中心(elasticjob/spark/flink/mr)、统一文档中心(swagger/rap)、统一开发中心（lcdp/autoGen），统一私有对象存储管理、统一库管理(git/nexus(maven/image))、Shell-Cli、多种工具链(hdfs/hbase/phoenix/oss)、即时通讯、轻量级风控等


[![Build Status](https://travis-ci.org/wl4g/dopaas.svg)](https://travis-ci.org/wl4g/dopaas)
![License](https://img.shields.io/badge/license-Apache2.0+-green.svg)
[![Maven](https://img.shields.io/badge/Maven-3.5+-green.svg)](https://github.com/wl4g/dopaas)
[![JDK](https://img.shields.io/badge/JDK-1.8+-green.svg)](https://github.com/wl4g/dopaas)
[![MySQL](https://img.shields.io/badge/MySQL-5.6+-green.svg)](https://github.com/wl4g/dopaas)
[![Redis](https://img.shields.io/badge/RedisCluster-3+-green.svg)](https://github.com/wl4g/dopaas)
[![Kafka](https://img.shields.io/badge/Kafka-0.10.0+-green.svg)](https://github.com/wl4g/dopaas)
[![Zookeeper](https://img.shields.io/badge/Zookeeper-3.4.6+-green.svg)](https://github.com/wl4g/dopaas)
[![Docker-ce](https://img.shields.io/badge/DockerCE-18.06+-green.svg)](https://github.com/wl4g/dopaas)
[![QQ1](https://img.shields.io/badge/QQ1-855349515-green.svg)](https://shang.qq.com/wpa/qunwpa?idkey=0343b06591d19188d86dc078912adfc5c40f023c8ec5a0d1eda5bdfc35ab40d0)
[![GithubStars](https://img.shields.io/github/stars/wl4g/dopaas)](https://github.com/wl4g/dopaas)
[![GiteeStars](https://gitee.com/wl4g/dopaas/badge/star.svg)](https://gitee.com/wl4g/dopaas)
[![Ubuntu](https://img.shields.io/badge/Ubuntu-16+-green.svg)](https://gitee.com/wl4g/dopaas)
[![CentOS](https://img.shields.io/badge/CentOS-6.5+-green.svg)](https://gitee.com/wl4g/dopaas)


## 1. 开发及运行时技术栈(主要)
本项目主要基于 SpringBoot/Cloud/Dubbo 开发，支持传统单体 (`standalone`) 模式 和 完全分布式微服务 (`cluster`) 模式运行，源码结构被精心设计为&nbsp;<b>既是平台又是框架的示范</b>.

- 必须依赖:  
Spring Boot:2.3 +  
Spring Cloud:Hoxton.x +  
Eureka:1.10 +  
Zipkin:2.15 +  
Jdk:8 +  
Maven:3.5 +  
Mysql:5.6 +  
- 可选依赖:  
Kafka:2.3.1 +  
Zookeeper:3.6.0 +  
DockerCE:18.06 +  
CoreDNS:1.7.0 +  
MinIO:latest  
Elasticsearch(EFK):6.2.3 +  
Shardingsphere-Elasticjob:3.0.0 +  

- [二次开发指南](README_DEVEL_CN.md)

## 2. 部署与搭建
### 2.1 一键自动部署
适用于主机快速部署，部署脚本会自动探测安装包括：jdk、nginx、git、maven、node、eureka、zookeeper、及所有SpringBoot apps，但不包括 redis/redis cluster(选其一)、Mysql服务，这些需要您自行手动安装.

```
bash -c "$(curl -L https://raw.githubusercontent.com/wl4g/dopaas/master/script/deploy/deploy-boot.sh)"
# 或
bash -c "$(curl -L https://gitee.com/wl4g/dopaas/raw/master/script/deploy/deploy-boot.sh)"
```
- 请注意按脚本提示，修改 [`deploy-env.sh`](script/deploy/deploy-env.sh) 自定义环境配置然后重新执行 [`./deploy-boot.sh`](script/deploy/deploy-boot.sh) , 由于是从源码编译部署, 请耐心等待大约 8~20min (第一次因需下载代码和依赖库会比较慢, 后续如需重新部署可根据实际情况设置 `export buildForcedOnPullUpToDate=true` 或 `export deployFrontendSkip=true` 来跳过后端编译和前端等来加速).  
- 部署前先检查目标主机上是否已安装 nginx/zookeeper/eureka 等, 若有则建议先停止, 因为自动部署也会部署避免冲突.

集群管理工具:  
[`./dopaas-ctl.sh`](script/deploy/dopaas-ctl.sh)
```
Usage: {start-all|stop-all|restart-all|status-all|<appName> <start|stop|restart|status>}
    start-all    Start all remote nodes apps.
     stop-all    Stop all remote nodes apps.
  restart-all    Restart all remote nodes apps.
   status-all    Query status all remote nodes apps.
    <appName> <start|stop|restart|status>   for example: {cmdb-facade restart}
        Restart only the cmdb-facade application of all remote nodes,
        The optionals are: zookeeper/eureka-server/iam-web/iam-facade/iam-data/cmdb-facade/cmdb-manager/... etc.
```

建议配置

| 运行模式 | 最低配置要求 | 说明 |
| ---- | ---- | ---- |
| cluster | CentOS7+ / Ubuntu18+ (4C+8G+) | 当只有1台机时即为伪集群 |
| standalone | 2C+4G+ | 单体应用 |


### 2.2 手动编译部署
适用于本地二次开发环境的搭建

#### 2.2.1 修改 .m2/settings.xml
```xml
<mirror>
    <id>nexus-aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Nexus aliyun</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public<url>
</mirror>
```

#### 2.2.2 后端编译
> 按项目依赖顺序进行编译, 其中 `mvn -P` 选项有：`springExecJar` (打包为单个可执行jar)、`mvnAssTar` (打包为通用软件发布包)，默认
```
git clone https://github.com/wl4g/component.git # 上游较新（推荐）
或者 git clone https://gitee.com/wl4g/component.git
mvn -f component -U clean install -DskipTests -T 2C

git clone https://github.com/wl4g/iam.git # 上游较新（推荐）
或者 git clone https://gitee.com/wl4g/iam.git
mvn -f iam -U clean install -DskipTests -T 2C

git clone https://github.com/wl4g/dopaas.git # 上游较新（推荐）
或者 git clone https://gitee.com/wl4g/dopaas.git
mvn -f dopaas -U clean install -DskipTests -T 2C
```

#### 2.2.3 前端编译
```
git clone https://github.com/wl4g/dopaas-view.git # 上游较新（推荐）
或者 git clone https://gitee.com/wl4g/dopaas-view.git
npm run dev # 开发调试
npm run build # 生产打包
```

### 2.3 初始数据库：首先准备一台MySQL5.6+实例，创建名为dopaas(utf8/utf8_bin)的库，再[初始数据库](../../../dopaas-db)。  
> 注：sql脚本需与源码版本对应，我们会定期更新，建议都使用最新 

### 2.4 配置DNS虚拟解析（C:\Windows\System32\drivers\etc 或 vim /etc/hosts）：  
[Standalone模式Hosts解析示例](dns/hosts.standalone.tpl)  
[Cluster模式Hosts解析示例](dns/hosts.cluster.tpl)  
> 注：对外的服务使用的域名在不同环境下请对应`sys_cluster_config.extranet_base_uri`表  

### 2.5 快速搭建redis/docker集群(或自行搭建)
[https://github.com/wl4g/docker-redis-cluster](https://github.com/wl4g/docker-redis-cluster) 或   
[https://gitee.com/wl4g/docker-redis-cluster](https://github.com/wl4g/docker-redis-cluster)  

### 2.6 启动与测试访问
说明：  
a. 尝试启动时无需指定任何jvm参数(即, 都使用默认配置), 如：默认仅会激活 `dev` 环境的配置;  
b. 无论是运行 `standalone` 模式还是 `cluster` 模式，都可以分别对应(`dev`/`fat`/`uat`/`pro`)4种环境。

#### 2.6.1 以 `standalone` 模式启动
只需运行以下启动类：

```
com.wl4g.StandaloneDopaas  
com.wl4g.StandaloneIam
```

#### 2.6.2 以 `cluster` 模式启动
请运行以下启动类：

```
com.wl4g.EurekaServer  
com.wl4g.IamWeb  
com.wl4g.IamFacade  
com.wl4g.IamData  
com.wl4g.CmdbFacade  
com.wl4g.CmdbManager  
com.wl4g.HomeFacade  
com.wl4g.HomeManager  
com.wl4g.LcdpFacade  
com.wl4g.LcdpManager  
com.wl4g.UciFacade  
com.wl4g.UciServer  
com.wl4g.UdmFacade  
com.wl4g.UdmManager  
com.wl4g.UdsFacade  
com.wl4g.UdsManager 
com.wl4g.UmcFacade 
com.wl4g.UmcManager  
com.wl4g.UmcTracker 
com.wl4g.UmcCollector 
com.wl4g.UosFacade  
com.wl4g.UosManager  
com.wl4g.UrmFacade  
com.wl4g.UrmManager  
```

#### 2.6.3 测试访问(推荐Chrome)
http://wl4g.debug &nbsp;&nbsp; 默认账号密码：root/wl4g.com

### 2.7 部署成功示例部分截图
> 提示：截图可能由于版本演变存在略微差异，如有问题请加交流群(联系在后面部分)

![registered-eureka-apps](shots/registered-eureka-apps.png)
- [更多截图](shots/)


## 3. 子模块文档
- [UCI](../../blob/master/dopaas-uci/README_CN.md)  (Unified Continuous Integration)统一持续集成交付服务, CI/CD等
- [UMC](../../blob/master/dopaas-umc/README_CN.md)  (Unified Monitoring and Operation Center)统一监控运维中心, 应用健康实时监控、实时追踪、实时告警、ELK日志分析等
- [URM](../../blob/master/dopaas-urm/README_CN.md)  (Unified Repository Management)统一仓库管理, 源码仓库、构建包仓库、镜像仓库等
- [UCM](../../blob/master/dopaas-ucm/README_CN.md)  (Unified Config Management)统一配置管理服务, 在线配置热更新如：DataSource、RedisClient等
- [CMDB](../../blob/master/dopaas-cmdb/README_CN.md)  (Configuration Management Database)统一资产管理, 主机、密钥、审计、DNS等基础设施资产管理等
- [UDM](../../blob/master/dopaas-udm/README_CN.md)  (Unified Document Management)统一文档管理, API/Swagger在线文档/Office/Pdf等
- [LCDP](../../blob/master/dopaas-lcdp/README_CN.md)  (Low Code Development Platform)低代码开发中心, 多种语言/架构完整项目自动生成(如springcloud/dubbo/golang/python/vue/angularjs等)、WebIDE、及各种开发者工具链等
- [UDS](../../blob/master/dopaas-uds/README_CN.md)  (Unified Distributed Scheduler)统一分布式调度管控中心, 如 支持ElasticJob任务、spark/flink任务等
- [UOS](../../blob/master/dopaas-uos/README_CN.md)  (Unified Object Storage)统一对象存储服务，基于springcloud开发支持NativeFS、HDFS、Aliyun OSS、AWS S3、GlusterFS等, Aliyun OSS, AWS S3, GlusterFS 等
- [UIM](../../blob/master/dopaas-uim/README_CN.md)  (Unified Instant Messaging)统一的即时通讯服务，方便项目人员及时沟通，安全分发内部资料.


## 4. 其他相关应用/组件文档
- [SHELL](dopaas-shell/README_CN.md)         Shell Cli, 给应用添加类似hbase-shell的控制台功能
- [IAM](iam/README_CN.md)					统一身份识别与访问管理服务，支持SSO/CAS、oauth2、opensaml等，同时支持多种部署模式（local/cluster/gateway）
- [Gateway](gateway/README_CN.md)			基于spring-cloud-gateway的企业级微服务网关, 可与CI整合实现金丝雀部署等等高级特性.


## 5. 如何交流、反馈、参与贡献？
- 点击入群 [![QQ1](https://img.shields.io/badge/QQ1-855349515-green.svg)](https://shang.qq.com/wpa/qunwpa?idkey=0343b06591d19188d86dc078912adfc5c40f023c8ec5a0d1eda5bdfc35ab40d0)
- ![q855349515](shots/q855349515.jpg)
- GitHub：https://github.com/wl4g/dopaas
- 开源中国：https://gitee.com/wl4g/dopaas
- 一个人的个人能力再强，也无法战胜一个团队，希望兄弟姐妹的支持，能够贡献出自己的部分代码，参与进来共同完善它(^_^)。

[如何共享代码](https://www.cnblogs.com/wenber/p/3630921.html)
