# AI Knowledge Base — API 设计

> 版本：v1.0  |  日期：2026-07-02  |  作者：MJ686

---

## 1. API 总览

### 1.1 接口规范

| 项目 | 规范 |
|------|------|
| 基础路径 | `/api` |
| 风格 | RESTful |
| 数据格式 | JSON (Content-Type: application/json) |
| 认证方式 | JWT Token (Header: `Authorization: Bearer <token>`) |
| 分页参数 | `pageNum` (页码，从1开始)、`pageSize` (每页条数) |
| 时间格式 | ISO 8601 (`2026-07-02T10:30:00`) |

### 1.2 统一响应体

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

**错误码约定：**

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证（Token 无效或过期） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 500 | 服务器内部错误 |
| 1001 | 用户名或密码错误 |
| 1002 | 账号已被禁用 |
| 2001 | 知识库不存在 |
| 2002 | 文档处理失败 |
| 3001 | AI 模型调用失败 |
| 3002 | 对话频率限制 |

### 1.3 分页响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [],
        "total": 100,
        "pageNum": 1,
        "pageSize": 10,
        "pages": 10
    }
}
```

---

## 2. 接口清单

### 2.1 认证模块 (Auth)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|:---:|
| POST | `/api/auth/register` | 用户注册 | ❌ |
| POST | `/api/auth/login` | 用户登录 | ❌ |
| POST | `/api/auth/refresh` | 刷新 Token | ✅ |
| POST | `/api/auth/logout` | 退出登录 | ✅ |

#### POST /api/auth/register

```
请求:
{
    "username": "zhangsan",
    "password": "Abc@1234",
    "nickname": "张三",
    "email": "zhangsan@example.com"     // 可选
}

成功响应 (200):
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "user": {
            "id": 1,
            "username": "zhangsan",
            "nickname": "张三",
            "role": "USER"
        }
    }
}

失败响应 (409):
{
    "code": 409,
    "message": "用户名已存在",
    "data": null
}
```

#### POST /api/auth/login

```
请求:
{
    "username": "zhangsan",
    "password": "Abc@1234"
}

成功响应 (200):
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "user": {
            "id": 1,
            "username": "zhangsan",
            "nickname": "张三",
            "avatar": null,
            "role": "USER"
        }
    }
}
```

---

### 2.2 用户模块 (User)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|:---:|
| GET | `/api/user/me` | 获取当前用户信息 | ✅ |
| PUT | `/api/user/me` | 更新个人信息 | ✅ |
| PUT | `/api/user/password` | 修改密码 | ✅ |
| PUT | `/api/user/avatar` | 上传头像 | ✅ |

#### GET /api/user/me

```
响应 (200):
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "email": "zhangsan@example.com",
        "avatar": "/uploads/avatar/1.jpg",
        "role": "USER",
        "createTime": "2026-07-01T10:00:00"
    }
}
```

#### PUT /api/user/password

```
请求:
{
    "oldPassword": "Abc@1234",
    "newPassword": "NewPass@5678"
}
```

---

### 2.3 知识库模块 (Knowledge)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|:---:|
| POST | `/api/knowledge` | 创建知识库 | ✅ |
| GET | `/api/knowledge` | 查询知识库列表(分页) | ✅ |
| GET | `/api/knowledge/{id}` | 知识库详情 | ✅ |
| PUT | `/api/knowledge/{id}` | 更新知识库 | ✅ |
| DELETE | `/api/knowledge/{id}` | 删除知识库(级联) | ✅ |
| GET | `/api/knowledge/{id}/stats` | 知识库统计信息 | ✅ |

#### POST /api/knowledge

```
请求:
{
    "name": "Spring AI 学习笔记",
    "description": "Spring AI 框架学习过程中整理的笔记和文档",
    "embeddingModel": "text-embedding-3-small"    // 可选，默认值
}

响应 (200):
{
    "code": 200,
    "message": "创建成功",
    "data": {
        "id": 1,
        "name": "Spring AI 学习笔记",
        "description": "Spring AI 框架学习过程中整理的笔记和文档",
        "isPublic": false,
        "docCount": 0,
        "chunkCount": 0,
        "embeddingModel": "text-embedding-3-small",
        "status": 1,
        "createTime": "2026-07-02T10:00:00"
    }
}
```

#### GET /api/knowledge

```
查询参数:
  pageNum=1&pageSize=10&keyword=Spring

响应 (200):
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "name": "Spring AI 学习笔记",
                "description": "...",
                "docCount": 5,
                "chunkCount": 128,
                "createTime": "2026-07-02T10:00:00"
            }
        ],
        "total": 3,
        "pageNum": 1,
        "pageSize": 10,
        "pages": 1
    }
}
```

---

### 2.4 文档模块 (Document)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|:---:|
| POST | `/api/knowledge/{kid}/documents` | 上传文档 | ✅ |
| GET | `/api/knowledge/{kid}/documents` | 文档列表(分页) | ✅ |
| GET | `/api/documents/{id}` | 文档详情 | ✅ |
| DELETE | `/api/documents/{id}` | 删除文档 | ✅ |
| POST | `/api/documents/{id}/reprocess` | 重新处理文档 | ✅ |
| GET | `/api/documents/{id}/chunks` | 查看分块列表 | ✅ |

#### POST /api/knowledge/{kid}/documents

```
Content-Type: multipart/form-data

请求:
  file: [文件]                   // 支持 PDF/DOCX/TXT/MD/CSV，最大50MB

响应 (200):
{
    "code": 200,
    "message": "上传成功，正在处理",
    "data": {
        "id": 1,
        "knowledgeId": 1,
        "fileName": "SpringAI-Guide.pdf",
        "fileType": "pdf",
        "fileSize": 2048576,
        "status": 0,
        "statusText": "待处理",
        "createTime": "2026-07-02T10:30:00"
    }
}
```

#### GET /api/knowledge/{kid}/documents

```
查询参数:
  pageNum=1&pageSize=10&status=3    // 按状态筛选

响应 (200):
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "fileName": "SpringAI-Guide.pdf",
                "fileType": "pdf",
                "fileSize": 2048576,
                "chunkCount": 32,
                "charCount": 45000,
                "status": 3,
                "statusText": "已完成",
                "createTime": "2026-07-02T10:30:00"
            }
        ],
        "total": 5,
        "pageNum": 1,
        "pageSize": 10,
        "pages": 1
    }
}
```

#### GET /api/documents/{id}/chunks

```
查询参数:
  pageNum=1&pageSize=20

响应 (200):
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "chunkIndex": 0,
                "content": "Spring AI 是 Spring 生态中的 AI 框架...",
                "tokenCount": 450,
                "charCount": 1800,
                "metadata": {"page": 1, "title": "概述"}
            }
        ],
        "total": 32,
        "pageNum": 1,
        "pageSize": 20,
        "pages": 2
    }
}
```

---

### 2.5 AI 对话模块 (Chat)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|:---:|
| POST | `/api/chat/sessions` | 创建会话 | ✅ |
| GET | `/api/chat/sessions` | 会话列表 | ✅ |
| PUT | `/api/chat/sessions/{id}` | 更新会话(重命名/参数) | ✅ |
| DELETE | `/api/chat/sessions/{id}` | 删除会话 | ✅ |
| GET | `/api/chat/sessions/{id}/messages` | 消息历史 | ✅ |
| POST | `/api/chat/send` | 发送消息(SSE 流式) | ✅ |

#### POST /api/chat/sessions

```
请求:
{
    "knowledgeId": 1,               // 可选，NULL=通用对话
    "title": "Spring AI 技术咨询",   // 可选
    "modelName": "gpt-4o-mini",     // 可选
    "temperature": 0.7,             // 可选
    "topK": 5                       // 可选，仅 RAG 对话
}

响应 (200):
{
    "code": 200,
    "message": "创建成功",
    "data": {
        "id": 1,
        "knowledgeId": 1,
        "knowledgeName": "Spring AI 学习笔记",
        "title": "Spring AI 技术咨询",
        "modelName": "gpt-4o-mini",
        "temperature": 0.7,
        "createTime": "2026-07-02T11:00:00"
    }
}
```

#### POST /api/chat/send（核心接口，SSE 流式）

```
请求:
{
    "sessionId": 1,
    "message": "什么是 RAG？请结合我的知识库内容回答"
}

响应: Content-Type: text/event-stream

// 1. 引用来源（RAG 模式才有）
event: reference
data: {"type":"reference","data":[{"chunkId":42,"documentId":7,"fileName":"RAG原理.md","content":"RAG是检索增强生成...","score":0.92}]}

// 2. 流式文本块
event: message
data: {"type":"chunk","data":"RAG（Retrieval-Augmented Generation）"}

event: message
data: {"type":"chunk","data":"是检索增强生成的缩写，"}

event: message
data: {"type":"chunk","data":"它结合了信息检索和文本生成两个阶段..."}

// 3. 完成
event: done
data: {"type":"done","data":{"messageId":100,"tokenCount":320}}

// 4. 错误（如发生）
event: error
data: {"type":"error","data":{"code":3001,"message":"AI模型调用超时"}}
```

#### GET /api/chat/sessions/{id}/messages

```
查询参数:
  pageNum=1&pageSize=20

响应 (200):
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "role": "user",
                "content": "什么是 RAG？",
                "createTime": "2026-07-02T11:01:00"
            },
            {
                "id": 2,
                "role": "assistant",
                "content": "RAG（Retrieval-Augmented Generation）是...",
                "tokenCount": 320,
                "references": [
                    {"chunkId": 42, "fileName": "RAG原理.md", "score": 0.92}
                ],
                "createTime": "2026-07-02T11:01:05"
            }
        ],
        "total": 4,
        "pageNum": 1,
        "pageSize": 20,
        "pages": 1
    }
}
```

---

### 2.6 管理后台模块 (Admin)

| 方法 | 路径 | 说明 | 认证 | 角色 |
|------|------|------|:---:|:---:|
| GET | `/api/admin/dashboard` | 仪表盘数据 | ✅ | ADMIN |
| GET | `/api/admin/users` | 用户列表(分页) | ✅ | ADMIN |
| PUT | `/api/admin/users/{id}/status` | 启用/禁用用户 | ✅ | ADMIN |
| GET | `/api/admin/logs` | 操作日志(分页) | ✅ | ADMIN |

#### GET /api/admin/dashboard

```
响应 (200):
{
    "code": 200,
    "message": "success",
    "data": {
        "userCount": 128,
        "knowledgeCount": 56,
        "documentCount": 342,
        "chatCount": 1024,
        "totalTokens": 1560000,
        "recentUsers": [
            {"date": "2026-07-01", "count": 5},
            {"date": "2026-07-02", "count": 8}
        ],
        "recentChats": [
            {"date": "2026-07-01", "count": 45},
            {"date": "2026-07-02", "count": 62}
        ]
    }
}
```

---

## 3. 接口总览表

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **认证** | POST | /api/auth/register | 注册 |
| | POST | /api/auth/login | 登录 |
| | POST | /api/auth/refresh | 刷新Token |
| | POST | /api/auth/logout | 退出 |
| **用户** | GET | /api/user/me | 当前用户信息 |
| | PUT | /api/user/me | 更新个人信息 |
| | PUT | /api/user/password | 修改密码 |
| | PUT | /api/user/avatar | 上传头像 |
| **知识库** | POST | /api/knowledge | 创建知识库 |
| | GET | /api/knowledge | 知识库列表 |
| | GET | /api/knowledge/{id} | 知识库详情 |
| | PUT | /api/knowledge/{id} | 更新知识库 |
| | DELETE | /api/knowledge/{id} | 删除知识库 |
| | GET | /api/knowledge/{id}/stats | 知识库统计 |
| **文档** | POST | /api/knowledge/{kid}/documents | 上传文档 |
| | GET | /api/knowledge/{kid}/documents | 文档列表 |
| | GET | /api/documents/{id} | 文档详情 |
| | DELETE | /api/documents/{id} | 删除文档 |
| | POST | /api/documents/{id}/reprocess | 重新处理 |
| | GET | /api/documents/{id}/chunks | 分块列表 |
| **对话** | POST | /api/chat/sessions | 创建会话 |
| | GET | /api/chat/sessions | 会话列表 |
| | PUT | /api/chat/sessions/{id} | 更新会话 |
| | DELETE | /api/chat/sessions/{id} | 删除会话 |
| | GET | /api/chat/sessions/{id}/messages | 消息历史 |
| | POST | /api/chat/send | 发送消息(SSE) |
| **管理** | GET | /api/admin/dashboard | 仪表盘 |
| | GET | /api/admin/users | 用户管理 |
| | PUT | /api/admin/users/{id}/status | 启用/禁用 |
| | GET | /api/admin/logs | 操作日志 |

**共计：28 个接口**
