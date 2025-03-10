# 在线小说阅读与发布平台

## 项目简介

本项目是一个在线小说阅读与发布平台，支持用户查询、阅读小说，提供各类排行榜，作家可以发布小说和修改章节内容。

## 功能特性

- **小说查询与阅读**：用户可以通过搜索或排行榜查找小说，并在线阅读章节内容。
- **排行榜系统**：提供多种排行榜，如热门书籍、最新更新、点击量排行等。
- **作家管理**：作家可以发布小说、更新章节，并管理自己的作品。
- **搜索功能**：支持按关键字搜索书籍，并进行多条件筛选。
- **点击量统计**：统计书籍点击量，并用于排行榜计算和热点小说发现。

## 技术架构

### 数据存储与分库分表

- **MySQL 垂直分库**：章节内容和其他业务表分离，避免数据增长影响查询性能。
- **MySQL 水平分库 & 分表**：章节内容表分散到多个数据库实例，以减少存储与访问压力。
- **Sharding-JDBC**：用于 SQL 路由，自动定位数据所在的数据库和表。

### 缓存优化

- **Caffeine & Redis**：缓存首页书本榜单和热门书籍，将查询 TPS 提升至 **1000**，接口响应时间稳定在 **10ms**。
- **Redis 书籍点击量缓存**：利用 XXL-JOB 定期将点击量持久化到数据库，并更新热门书籍榜单。

### 搜索 & 消息队列

- **ElasticSearch**：支持按关键字搜索书籍，并提供多条件筛选排行榜功能。
- **RabbitMQ**：同步书籍、章节内容等数据变化至 Caffeine、Redis 和 ElasticSearch，确保数据一致性和实时性。

### 并发优化 & ID 生成

- **CompletableFuture 并发优化**：优化首页、书籍信息和章节内容查询，提高并发性能。
- **Redis 雪花算法**：基于 Redis 生成全局唯一用户 ID，确保高效 ID 生成。

### 静态资源存储

- **阿里云 OSS**：用于存储用户头像、书籍封面等静态资源，可通过配置切换至本地存储。

