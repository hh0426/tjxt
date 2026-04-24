# 天机学堂 (Tianji Education Platform)

<div align="center">

![Java](https://img.shields.io/badge/Java-11-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.2-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.3-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**基于 Spring Cloud Alibaba 微服务架构的在线教育平台**

</div>

## 📖 项目简介

天机学堂是一个功能完善的在线教育平台，采用前后端分离的微服务架构设计。项目基于 Spring Cloud Alibaba 技术栈构建，提供了课程管理、在线学习、考试测评、交易支付、数据分析等完整的在线教育解决方案。

### ✨ 核心特性

- 🏗️ **微服务架构**: 基于 Spring Cloud Alibaba 的分布式微服务系统
- 🔐 **统一认证**: JWT + OAuth2 实现统一的身份认证与授权
- 📚 **课程管理**: 支持课程发布、分类管理、章节编排、多媒体内容
- 🎓 **在线学习**: 学习计划、学习进度跟踪、视频播放记录
- 📝 **考试系统**: 题库管理、在线考试、自动阅卷
- 💳 **交易系统**: 购物车、订单管理、支付集成（支付宝）、退款处理
- 🔍 **全文搜索**: 基于 Elasticsearch 的课程搜索与推荐
- 📊 **数据统计**: 多维度数据分析与可视化展示
- 💬 **消息通知**: 基于 RabbitMQ 的异步消息处理
- 🎥 **媒体服务**: 集成腾讯云 VOD 视频点播服务

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 11 | 开发语言 |
| Spring Boot | 2.7.2 | 基础框架 |
| Spring Cloud | 2021.0.3 | 微服务框架 |
| Spring Cloud Alibaba | 2021.0.1.0 | 阿里微服务套件 |
| Nacos | - | 服务注册发现 & 配置中心 |
| OpenFeign | - | 服务间调用 |
| Gateway | - | API 网关 |
| Sentinel | - | 流量控制 & 熔断降级 |
| Seata | 1.5.1 | 分布式事务 |
| MyBatis Plus | 3.4.3 | ORM 框架 |
| MySQL | 8.0.23 | 关系型数据库 |
| Redis | - | 缓存数据库 |
| Redisson | 3.13.6 | Redis 客户端 |
| RabbitMQ | - | 消息队列 |
| Elasticsearch | 7.12.1 | 搜索引擎 |
| XXL-Job | 2.3.1 | 分布式任务调度 |
| Knife4j | 3.0.3 | API 文档工具 |
| Hutool | 5.7.17 | Java 工具库 |
| Lombok | 1.18.20 | 代码简化 |

### 第三方服务

- **腾讯云**: COS 对象存储、VOD 视频点播
- **阿里云**: OSS 对象存储、KMS 密钥管理
- **支付宝**: 支付 SDK

### 前端技术

- Vue.js / React (前端项目独立部署)

### 运维技术

- Docker: 容器化部署
- Maven: 项目构建管理

## 📦 项目结构

```
tianji/
├── tj-common/              # 公共模块（工具类、常量、异常处理等）
├── tj-api/                 # API 接口定义（DTO、Feign Client）
├── tj-auth/                # 认证授权服务
│   ├── tj-auth-common/     # 认证公共组件
│   ├── tj-auth-gateway-sdk/# 网关认证 SDK
│   ├── tj-auth-resource-sdk/# 资源服务认证 SDK
│   └── tj-auth-service/    # 认证服务主程序
├── tj-gateway/             # API 网关服务
├── tj-user/                # 用户服务
├── tj-course/              # 课程服务
├── tj-learning/            # 学习服务
├── tj-exam/                # 考试服务
├── tj-search/              # 搜索服务
├── tj-trade/               # 交易服务
├── tj-pay/                 # 支付服务
│   ├── tj-pay-api/         # 支付 API
│   ├── tj-pay-domain/      # 支付领域模型
│   └── tj-pay-service/     # 支付服务
├── tj-media/               # 媒体服务
├── tj-message/             # 消息服务
│   ├── tj-message-api/     # 消息 API
│   ├── tj-message-domain/  # 消息领域模型
│   └── tj-message-service/ # 消息服务
├── tj-remark/              # 评论服务
├── tj-data/                # 数据中心服务
├── Dockerfile              # Docker 配置文件
└── pom.xml                 # Maven 父 POM
```

## 🚀 核心功能模块

### 1. 用户服务 (tj-user)
- 用户注册与登录
- 学员信息管理
- 教师信息管理
- 角色权限管理

### 2. 课程服务 (tj-course)
- 课程 CRUD 操作
- 课程分类管理（三级分类）
- 课程章节编排
- 课程草稿管理
- 课程上架/下架
- 教师管理

### 3. 学习服务 (tj-learning)
- 学习计划管理
- 学习进度跟踪
- 学习记录保存
- 视频播放进度同步
- 课程收藏功能

### 4. 考试服务 (tj-exam)
- 题库管理（单选、多选、判断、填空、简答）
- 试卷管理
- 在线考试
- 自动阅卷
- 成绩查询

### 5. 交易服务 (tj-trade)
- 购物车管理
- 订单创建与管理
- 订单支付状态跟踪
- 退款申请与审批
- 报名统计

### 6. 支付服务 (tj-pay)
- 支付宝支付集成
- 支付回调处理
- 退款处理
- 多渠道支付支持

### 7. 搜索服务 (tj-search)
- 基于 Elasticsearch 的课程搜索
- 关键词高亮
- 搜索结果排序与过滤
- 搜索建议

### 8. 媒体服务 (tj-media)
- 视频上传与转码
- 腾讯云 VOD 集成
- 对象存储（COS/OSS）
- 媒体文件管理

### 9. 消息服务 (tj-message)
- 站内消息
- 短信通知
- 邮件通知
- 消息模板管理

### 10. 评论服务 (tj-remark)
- 课程评论
- 评论回复
- 评论点赞
- 评论审核

### 11. 数据中心 (tj-data)
- 学习数据统计
- 销售数据分析
- 用户行为分析
- 数据报表生成

### 12. API 网关 (tj-gateway)
- 路由转发
- 负载均衡
- 身份认证
- 权限校验
- 限流熔断

### 13. 认证服务 (tj-auth)
- JWT Token 生成与验证
- OAuth2 认证流程
- 用户权限管理
- 单点登录支持

## 🔧 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- RabbitMQ 3.8+
- Elasticsearch 7.12+
- Nacos 2.0+

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-username/tianji.git
cd tianji
```

#### 2. 配置 Nacos

启动 Nacos 服务，并在 Nacos 控制台配置各服务的配置文件。

#### 3. 初始化数据库

执行 SQL 脚本初始化各服务对应的数据库。

#### 4. 修改配置

根据实际环境修改 `bootstrap-dev.yml` 或 `bootstrap-local.yml` 中的配置：
- 数据库连接信息
- Redis 连接信息
- RabbitMQ 连接信息
- Elasticsearch 地址
- Nacos 地址
- 第三方服务密钥（腾讯云、阿里云、支付宝）

#### 5. 编译项目

```bash
mvn clean install -DskipTests
```

#### 6. 启动服务

按以下顺序启动微服务：

1. tj-auth-service（认证服务）
2. tj-gateway（网关服务）
3. tj-user（用户服务）
4. tj-course（课程服务）
5. tj-learning（学习服务）
6. tj-exam（考试服务）
7. tj-search（搜索服务）
8. tj-trade（交易服务）
9. tj-pay（支付服务）
10. tj-media（媒体服务）
11. tj-message（消息服务）
12. tj-remark（评论服务）
13. tj-data（数据中心）

```bash
cd tj-auth/tj-auth-service
mvn spring-boot:run

# 在其他终端窗口依次启动其他服务
```

#### 7. 访问 API 文档

启动服务后，访问 Knife4j 文档：

```
http://localhost:{port}/doc.html
```

### Docker 部署

```bash
# 构建镜像
docker build -t tianji-service .

# 运行容器
docker run -d \
  --name tianji-service \
  -p 8080:8080 \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  tianji-service
```

## 📊 系统架构

### 架构图

```
                    ┌─────────────┐
                    │   Client    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │   Gateway   │
                    │  (API 网关)  │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼───────┐  ┌──────▼──────┐  ┌───────▼───────┐
│ Auth Service  │  │ User Service│  │Course Service │
│  (认证服务)    │  │  (用户服务)  │  │  (课程服务)    │
└───────────────┘  └─────────────┘  └───────────────┘
        │                  │                  │
┌───────▼───────┐  ┌──────▼──────┐  ┌───────▼───────┐
│Learning Svc   │  │ Exam Service│  │Search Service │
│  (学习服务)    │  │  (考试服务)  │  │  (搜索服务)    │
└───────────────┘  └─────────────┘  └───────────────┘
        │                  │                  │
┌───────▼───────┐  ┌──────▼──────┐  ┌───────▼───────┐
│Trade Service  │  │ Pay Service │  │Media Service  │
│  (交易服务)    │  │  (支付服务)  │  │  (媒体服务)    │
└───────────────┘  └─────────────┘  └───────────────┘
        │                  │                  │
┌───────▼───────┐  ┌──────▼──────┐  ┌───────▼───────┐
│Message Svc    │  │Remark Svc   │  │ Data Center   │
│  (消息服务)    │  │  (评论服务)  │  │  (数据中心)    │
└───────────────┘  └─────────────┘  └───────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼───────┐  ┌──────▼──────┐  ┌───────▼───────┐
│    MySQL      │  │    Redis    │  │     MQ        │
│  (数据库)      │  │   (缓存)    │  │  (消息队列)    │
└───────────────┘  └─────────────┘  └───────────────┘
        │
┌───────▼───────┐
│Elasticsearch  │
│  (搜索引擎)    │
└───────────────┘
```

### 技术架构说明

- **服务注册与发现**: Nacos
- **配置中心**: Nacos Config
- **服务调用**: OpenFeign + LoadBalancer
- **网关路由**: Spring Cloud Gateway
- **认证授权**: JWT + OAuth2
- **流量控制**: Sentinel
- **分布式事务**: Seata
- **消息队列**: RabbitMQ
- **缓存**: Redis + Redisson + Caffeine
- **搜索引擎**: Elasticsearch
- **任务调度**: XXL-Job
- **链路追踪**: Sleuth + Zipkin（可选）

## 🔐 安全设计

- JWT Token 认证机制
- OAuth2 授权流程
- 网关层统一鉴权
- 敏感数据加密存储
- API 接口签名验证
- 防重放攻击机制

## 📈 性能优化

- Redis 多级缓存策略
- 本地缓存（Caffeine）
- 数据库连接池优化
- 异步消息处理
- 延迟队列实现
- 分页查询优化
- 索引优化

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 团队

**研发团队**: 传智教育·研究院

- 联系人: Zhang Huyi
- 邮箱: zhanghuyi@itcast.cn
- 官网: http://www.itcast.cn/

## 🙏 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba)
- [MyBatis Plus](https://baomidou.com/)
- [Nacos](https://nacos.io/)
- [Elasticsearch](https://www.elastic.co/elasticsearch/)

---

<div align="center">

**如果这个项目对您有帮助，请给个 ⭐ Star 支持一下！**

Made with ❤️ by Tianji Team

</div>
