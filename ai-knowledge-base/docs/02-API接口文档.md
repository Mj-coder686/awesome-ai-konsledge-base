# API 接口文档

> 项目：AI Knowledge Base
> 版本：v4.0 Final
> 更新：2026-07-02
> 测试工具：Postman / Apifox

---

## 1. 通用约定

### 1.1 基础信息

```
基础路径：/api
数据格式：JSON（Content-Type: application/json）
认证方式：JWT Token（Header: Authorization: Bearer <token>）
```

### 1.2 统一响应格式

所有接口统一返回以下结构：

```json
{
    "code": 200,
    "msg": "success",
    "data": {}
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 无效 |
| 500 | 服务器内部错误 |

### 1.3 需要认证的接口

在 Header 中带上：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx
```

---

## 2. 接口列表

| # | 方法 | 路径 | 说明 | 认证 |
|:-:|------|------|------|:---:|
| 1 | POST | /api/register | 注册 | ❌ |
| 2 | POST | /api/login | 登录 | ❌ |
| 3 | POST | /api/knowledge | 创建知识库 | ✅ |
| 4 | GET | /api/knowledge | 知识库列表 | ✅ |
| 5 | DELETE | /api/knowledge/{id} | 删除知识库 | ✅ |
| 6 | POST | /api/knowledge/{id}/documents | 上传文档 | ✅ |
| 7 | GET | /api/knowledge/{id}/documents | 文档列表 | ✅ |
| 8 | GET | /api/documents/{id} | 文档详情 | ✅ |

---

## 3. 接口详细说明

---

### 3.1 POST /api/register

**说明：** 用户注册

**请求：**
```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| username | String | ✅ | 用户名，3-50个字符 |
| password | String | ✅ | 密码，6-20个字符 |

**成功响应：**
```json
{
    "code": 200,
    "msg": "注册成功",
    "data": {
        "id": 1,
        "username": "zhangsan",
        "nickname": null
    }
}
```

**失败响应：**
```json
{
    "code": 400,
    "msg": "用户名已存在",
    "data": null
}
```

---

### 3.2 POST /api/login

**说明：** 用户登录，返回 JWT Token

**请求：**
```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

**成功响应：**
```json
{
    "code": 200,
    "msg": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiemhhbmdzYW4iLCJleHAiOjE3MjAwMDAwMDB9.xxxxx"
    }
}
```

**失败响应：**
```json
{
    "code": 400,
    "msg": "用户名或密码错误",
    "data": null
}
```

---

### 3.3 POST /api/knowledge

**说明：** 创建知识库

**请求头：**
```
Authorization: Bearer <token>
```

**请求：**
```json
{
    "name": "Spring AI 学习笔记",
    "description": "Spring AI 框架学习资料"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| name | String | ✅ | 知识库名称 |
| description | String | ❌ | 描述 |

**成功响应：**
```json
{
    "code": 200,
    "msg": "创建成功",
    "data": {
        "id": 1,
        "userId": 1,
        "name": "Spring AI 学习笔记",
        "description": "Spring AI 框架学习资料",
        "createTime": "2026-07-02T10:00:00"
    }
}
```

---

### 3.4 GET /api/knowledge

**说明：** 查询当前用户的知识库列表

**请求头：**
```
Authorization: Bearer <token>
```

**请求参数（Query）：**
```
无参数，返回当前用户的所有知识库
```

**成功响应：**
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "name": "Spring AI 学习笔记",
            "description": "Spring AI 框架学习资料",
            "createTime": "2026-07-02T10:00:00"
        },
        {
            "id": 2,
            "name": "Java 基础",
            "description": null,
            "createTime": "2026-07-02T11:00:00"
        }
    ]
}
```

---

### 3.5 DELETE /api/knowledge/{id}

**说明：** 删除知识库及其下所有文档

**请求头：**
```
Authorization: Bearer <token>
```

**路径参数：**
| 参数 | 说明 |
|------|------|
| id | 知识库ID |

**成功响应：**
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

**失败响应：**
```json
{
    "code": 400,
    "msg": "知识库不存在或无权删除",
    "data": null
}
```

---

### 3.6 POST /api/knowledge/{id}/documents

**说明：** 上传文档到指定知识库

**请求头：**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**路径参数：**
| 参数 | 说明 |
|------|------|
| id | 知识库ID |

**表单参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| file | File | ✅ | 上传的文件，支持 pdf/docx/txt/md |

**成功响应：**
```json
{
    "code": 200,
    "msg": "上传成功",
    "data": {
        "id": 1,
        "knowledgeId": 1,
        "fileName": "SpringAI-Guide.pdf",
        "fileType": "pdf",
        "fileSize": 2048576,
        "status": 0,
        "createTime": "2026-07-02T10:30:00"
    }
}
```

---

### 3.7 GET /api/knowledge/{id}/documents

**说明：** 查询指定知识库下的文档列表

**请求头：**
```
Authorization: Bearer <token>
```

**路径参数：**
| 参数 | 说明 |
|------|------|
| id | 知识库ID |

**成功响应：**
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "fileName": "SpringAI-Guide.pdf",
            "fileType": "pdf",
            "fileSize": 2048576,
            "status": 1,
            "createTime": "2026-07-02T10:30:00"
        },
        {
            "id": 2,
            "fileName": "notes.md",
            "fileType": "md",
            "fileSize": 10240,
            "status": 0,
            "createTime": "2026-07-02T11:00:00"
        }
    ]
}
```

---

### 3.8 GET /api/documents/{id}

**说明：** 查询文档详情

**请求头：**
```
Authorization: Bearer <token>
```

**路径参数：**
| 参数 | 说明 |
|------|------|
| id | 文档ID |

**成功响应：**
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1,
        "knowledgeId": 1,
        "fileName": "SpringAI-Guide.pdf",
        "fileType": "pdf",
        "fileSize": 2048576,
        "filePath": "./uploads/abc123.pdf",
        "status": 1,
        "errorMsg": null,
        "createTime": "2026-07-02T10:30:00"
    }
}
```

---

## 4. 接口测试顺序

```
Step 1: POST /api/register          → 注册一个用户
Step 2: POST /api/login             → 登录拿到 token
Step 3: POST /api/knowledge         → 创建一个知识库（带 token）
Step 4: GET /api/knowledge          → 查看知识库列表（带 token）
Step 5: POST /api/knowledge/1/documents → 上传一个文件（带 token，multipart）
Step 6: GET /api/knowledge/1/documents  → 查看文档列表（带 token）
Step 7: GET /api/documents/1        → 查看文档详情（带 token）
Step 8: DELETE /api/knowledge/1     → 删除知识库（带 token）
```

**全程用 Postman 测试，每完成一个接口就测一个。**
