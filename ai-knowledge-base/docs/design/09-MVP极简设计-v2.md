# AI Knowledge Base — MVP 极简设计 v2.0

> 目标：2 周跑通「上传文档 → AI 问答」核心链路
> 原则：能砍则砍，能硬编码就不配置，先跑起来再优化

---

## 一、砍掉了什么（vs v1.0 企业级设计）

| v1.0 设计 | MVP 决策 | 理由 |
|-----------|:--------:|------|
| 5 个业务模块 (auth/user/knowledge/ai/admin) | **2 个 (user + knowledge)** | auth 合入 user，admin 全砍，ai 逻辑合入 knowledge |
| 7 张数据库表 | **3 张** | chunk 表砍掉（向量数据只存 VectorStore），会话/消息表砍掉（不持久化） |
| 28 个 API 接口 | **7 个** | 只保留核心链路必须的接口 |
| 完整 RAG（混合检索 + Rerank + 引用溯源） | **基础 RAG**（纯向量检索 + Prompt 注入） | 先跑通，后优化 |
| SSE 流式输出 | **普通 JSON 返回** | 省掉 SSE 复杂度，先对齐功能 |
| Spring Security + JWT 完整认证 | **简单 JWT Filter** | 不用 Security 框架，手写一个 Filter 判 Token |
| 对话历史持久化 + Redis 缓存 | **不持久化，不缓存** | 每次请求自带历史（前端传），或只保持单轮 |
| 操作日志 / 管理后台 | **全砍** | MVP 不需要 |
| 文件存储 (MinIO) | **本地磁盘** | `./uploads/` 目录 |
| 多环境配置 (dev/prod) | **单环境 application.yml** | 先跑通 |
| Knife4j API 文档 | **砍掉** | Postman 够用 |
| Docker Compose 编排 | **砍掉** | 本地直接跑 |
| 前端 Vue 3 | **砍掉** | 用 Postman 测试，后端能跑就行 |

---

## 二、第一阶段必须实现清单（共 12 个任务）

### 任务清单（按开发顺序）

| # | 任务 | 产出 | 优先级 |
|:-:|------|------|:------:|
| 1 | 项目初始化 + pom.xml + 配置 | 项目可启动 | P0 |
| 2 | 建表 SQL（3 张表） | 数据库就绪 | P0 |
| 3 | User 表 + 注册接口 | POST /api/register | P0 |
| 4 | 登录接口 + JWT 生成 | POST /api/login | P0 |
| 5 | JWT 过滤器（手写 Filter） | 接口鉴权 | P0 |
| 6 | 创建知识库 + 知识库列表 | POST/GET /api/knowledge | P0 |
| 7 | 上传文档 + 存储文件 + 入库记录 | POST /api/knowledge/{id}/documents | P0 |
| 8 | 文档解析 (Tika) + 文本分块 | 异步处理链路 | P0 |
| 9 | Embedding + 向量存储 | 文档向量化入库 | P0 |
| 10 | 普通对话（无知识库） | POST /api/chat | P0 |
| 11 | RAG 对话（关联知识库） | POST /api/chat?knowledgeId=1 | P0 |
| 12 | 端到端联调 | 全链路通过 | P0 |

**全部 P0，没有 P1。12 个任务做完 = MVP 交付。**

---

## 三、数据库设计（3 张表）

```sql
CREATE DATABASE IF NOT EXISTS ai_knowledge_base DEFAULT CHARSET utf8mb4;
USE ai_knowledge_base;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE `user` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL UNIQUE,
    `password`    VARCHAR(255) NOT NULL              COMMENT 'BCrypt',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- 2. 知识库表
-- ============================================
CREATE TABLE `knowledge` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
);

-- ============================================
-- 3. 文档表（合并了文档 + 分块的概念）
-- ============================================
CREATE TABLE `document` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `knowledge_id`  BIGINT       NOT NULL,
    `file_name`     VARCHAR(255) NOT NULL,
    `file_type`     VARCHAR(20)  NOT NULL,
    `file_size`     BIGINT       NOT NULL,
    `file_path`     VARCHAR(500) NOT NULL,
    `chunk_count`   INT          NOT NULL DEFAULT 0,
    `status`        TINYINT      NOT NULL DEFAULT 0  COMMENT '0待处理 1处理中 2已完成 3失败',
    `error_msg`     VARCHAR(500) DEFAULT NULL,
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_knowledge_id` (`knowledge_id`)
);
```

**砍掉了什么：**
- `document_chunk` 表 → 不单独建表，分块内容直接写入 VectorStore，通过 metadata.documentId 关联
- `chat_session` / `chat_message` 表 → MVP 不持久化对话，前端传历史即可
- `operation_log` 表 → 砍掉
- `deleted` 逻辑删除 → MVP 直接物理删除
- 所有 VO/DTO 嵌套 → 直接用 Map 或简单对象返回

---

## 四、API 最小集合（7 个接口）

| # | 方法 | 路径 | 说明 | 认证 |
|:-:|------|------|------|:---:|
| 1 | POST | `/api/register` | 注册 | ❌ |
| 2 | POST | `/api/login` | 登录，返回 JWT | ❌ |
| 3 | POST | `/api/knowledge` | 创建知识库 | ✅ |
| 4 | GET | `/api/knowledge` | 我的知识库列表 | ✅ |
| 5 | POST | `/api/knowledge/{id}/documents` | 上传文档 | ✅ |
| 6 | GET | `/api/knowledge/{id}/documents` | 文档列表 | ✅ |
| 7 | POST | `/api/chat` | AI 对话（普通 + RAG） | ✅ |

### 接口详细设计

#### POST /api/register
```
请求:  { "username": "test", "password": "123456" }
响应:  { "code": 200, "msg": "ok", "data": { "id": 1, "username": "test" } }
```

#### POST /api/login
```
请求:  { "username": "test", "password": "123456" }
响应:  { "code": 200, "msg": "ok", "data": { "token": "eyJxxx..." } }
```

#### POST /api/knowledge
```
Header: Authorization: Bearer <token>
请求:  { "name": "Spring AI 笔记", "description": "学习资料" }
响应:  { "code": 200, "msg": "ok", "data": { "id": 1, "name": "Spring AI 笔记" } }
```

#### GET /api/knowledge
```
Header: Authorization: Bearer <token>
响应:  { "code": 200, "msg": "ok", "data": [{ "id": 1, "name": "...", "description": "..." }] }
```

#### POST /api/knowledge/{id}/documents
```
Header: Authorization: Bearer <token>
Content-Type: multipart/form-data
请求:  file=<PDF文件>
响应:  { "code": 200, "msg": "上传成功，正在处理", "data": { "id": 1, "fileName": "xxx.pdf", "status": 0 } }
```

#### GET /api/knowledge/{id}/documents
```
Header: Authorization: Bearer <token>
响应:  { "code": 200, "msg": "ok", "data": [{ "id": 1, "fileName": "xxx.pdf", "status": 2, "chunkCount": 15 }] }
```

#### POST /api/chat
```
Header: Authorization: Bearer <token>
请求:
{
    "message": "什么是RAG？",
    "knowledgeId": 1            // 可选，传了=RAG，不传=普通对话
}

响应:
{
    "code": 200,
    "msg": "ok",
    "data": {
        "answer": "RAG是检索增强生成（Retrieval-Augmented Generation）的缩写...",
        "references": [          // 仅 RAG 模式有
            { "content": "RAG的核心流程是...", "fileName": "xxx.pdf", "score": 0.92 }
        ]
    }
}
```

**SSE 流式？→ MVP 不做。** 普通 JSON 返回，前端 loading 等待即可。后续版本再加 SSE。

---

## 五、目录结构（极简，不分模块）

```
src/main/java/com/mj/aiknowledgebase/
│
├── AiKnowledgeBaseApplication.java          # 启动类
│
├── common/                                  # 公共组件（仅必要项）
│   ├── R.java                               # 统一响应 {code, msg, data}
│   ├── GlobalExceptionHandler.java          # 全局异常
│   └── JwtUtil.java                         # JWT 生成/解析
│
├── config/                                  # 配置
│   ├── WebConfig.java                       # CORS + 拦截器注册
│   ├── JwtFilter.java                       # JWT 过滤器
│   └── MyBatisPlusConfig.java               # 分页插件
│
├── controller/                              # 所有 Controller
│   ├── AuthController.java                  # 注册 + 登录
│   ├── KnowledgeController.java             # 知识库 CRUD + 文档上传列表
│   └── ChatController.java                  # AI 对话
│
├── service/                                 # 所有 Service
│   ├── UserService.java
│   ├── KnowledgeService.java
│   ├── DocumentService.java                 # 文档上传 + 解析 + 分块
│   ├── EmbeddingService.java                # 文本向量化
│   └── ChatService.java                     # 对话（普通 + RAG）
│
├── mapper/                                  # MyBatis Plus Mapper
│   ├── UserMapper.java
│   ├── KnowledgeMapper.java
│   └── DocumentMapper.java
│
└── entity/                                  # 数据库实体（PO = VO，不分开）
    ├── User.java
    ├── Knowledge.java
    └── Document.java
```

```
src/main/resources/
├── application.yml                          # 全部配置写这一个文件
├── schema.sql                               # 建表 SQL
└── prompts/                                 # Prompt 模板
    └── rag-system.txt                       # RAG 系统提示词
```

**没有的东西：**
- ❌ 没有 `dto/`、`vo/`、`po/` 三层分离 → 实体类直接当返回值用
- ❌ 没有 `module/` 子包 → 扁平结构，controller/service/mapper 各一层
- ❌ 没有 `aspect/` → 不做 AOP 日志
- ❌ 没有 `runner/` → 手动建管理员
- ❌ 没有 `parser/` 策略模式 → DocumentService 里直接写 if-else 判断类型
- ❌ 没有 MyBatis XML → MyBatis Plus 的 BaseMapper 足够

---

## 六、核心流程实现思路

### 6.1 文档上传 → 处理流程

```
Controller 收到文件
    │
    ├── 同步（立即返回）
    │   ├── 存文件到 ./uploads/
    │   ├── 插入 document 记录 (status=0)
    │   └── 返回响应
    │
    └── 异步 @Async（后台线程）
        ├── 更新 status=1
        ├── Tika 解析 → 得到纯文本
        ├── 简单分块（按 \n\n 切分，每块 ≤ 500 字符）
        ├── 调用 EmbeddingService 向量化每个分块
        ├── 向量写入 VectorStore（metadata 带 documentId + fileName + 原文）
        ├── 更新 document.chunk_count = 分块数
        └── 更新 status=2（或 status=3 如果失败）
```

### 6.2 RAG 对话流程

```
收到 { message, knowledgeId? }
    │
    ├── knowledgeId == null → 普通对话
    │   └── 直接调 ChatClient.call(message) → 返回
    │
    └── knowledgeId != null → RAG 对话
        ├── 把 message 做 Embedding
        ├── VectorStore.similaritySearch(query, topK=3, filter=document.knowledgeId)
        ├── 拿到相关 Chunk 原文
        ├── 组装 Prompt:
        │     System: "你是知识库助手，根据以下参考资料回答。"
        │     Context: "###\n{chunk1}\n###\n{chunk2}\n###"
        │     User: message
        ├── 调 ChatClient.call(prompt) → 返回
        └── 同时返回 references: [chunk1原文, chunk2原文, ...]
```

### 6.3 JWT 鉴权（极简版）

```java
// JwtFilter extends OncePerRequestFilter
// 只做一件事：从 Header 取 token → 解析 userId → 放入 request.setAttribute("userId", userId)
// 白名单：/api/register, /api/login 直接放行
// 不用 Security 框架，不配 SecurityConfig
```

---

## 七、pom.xml 最终依赖（仅需 12 个）

```xml
<properties>
    <java.version>21</java.version>
    <spring-ai.version>1.0.0</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    # 1. Web
    spring-boot-starter-web

    # 2. ORM
    mybatis-plus-spring-boot3-starter (3.5.7)
    mysql-connector-j (runtime)

    # 3. Redis
    spring-boot-starter-data-redis

    # 4. AI
    spring-ai-openai-spring-boot-starter

    # 5. 文档解析
    tika-core (2.9.2)

    # 6. JWT
    jjwt-api (0.12.6)
    jjwt-impl (0.12.6, runtime)
    jjwt-jackson (0.12.6, runtime)

    # 7. 工具
    lombok (optional)

    # 8. 测试
    spring-boot-starter-test (test)
</dependencies>
```

**移除了：** Security、AOP、Validation、Knife4j、restdocs、asciidoctor

---

## 八、application.yml（单文件，全部写死）

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_knowledge_base?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root

  data:
    redis:
      host: localhost
      port: 6379

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

  ai:
    openai:
      base-url: http://localhost:11434     # Ollama 本地
      api-key: ollama
      chat:
        options:
          model: qwen2.5:7b
          temperature: 0.7
      embedding:
        options:
          model: nomic-embed-text

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

# 文件上传路径
file:
  upload-dir: ./uploads

# JWT
jwt:
  secret: AiKnowledgeBaseSecretKey2026ThisIsAVeryLongKeyForHS256Algorithm
  expiration: 86400000   # 24小时
```

---

## 九、两周开发计划

### Week 1（Day 1-5）：后端骨架 + 文档处理

| 天 | 任务 | 产出 |
|:--:|------|------|
| D1 | pom.xml + application.yml + 建表 SQL + 项目启动 | 项目跑起来 |
| D2 | User 实体 + AuthController（注册/登录）+ JwtUtil + JwtFilter | 认证完成 |
| D3 | Knowledge 实体 + KnowledgeController（创建/列表）+ Document 实体 | 知识库和文档表 |
| D4 | DocumentService（上传文件 + Tika 解析 + 分块）+ @Async | 文档处理链路 |
| D5 | EmbeddingService + SimpleVectorStore + 入库向量化 | 向量化完成 |

### Week 2（Day 6-10）：AI 对话 + 联调

| 天 | 任务 | 产出 |
|:--:|------|------|
| D6 | ChatController + ChatService 普通对话 | AI 对话可用 |
| D7 | RAG 检索逻辑 + ChatService RAG 对话 | RAG 可用 |
| D8 | 联调：上传文档 → 向量化 → RAG 问答 全链路 | 端到端通过 |
| D9 | Bug 修复 + 边界处理 + 错误提示优化 | 稳定版本 |
| D10 | 整体测试 + README 更新 + 简单截图 | MVP 交付 |

---

## 十、MVP 完成后的下一步（v2.0）

```
MVP 完成后，按优先级依次加：

v2.0 - SSE 流式输出（体感提升最大）
v2.1 - 对话历史持久化（chat_session + chat_message 表）
v2.2 - 前端 Vue 3 页面
v2.3 - 管理后台（仪表盘 + 用户管理）
v2.4 - Docker 部署
v3.0 - Milvus 向量数据库升级
v3.1 - Agent + 工具调用
```

**每个版本都是可运行的增量，而不是一次性的大重构。**
