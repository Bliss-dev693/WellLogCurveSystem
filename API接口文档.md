# 测井曲线智能重构系统 API 接口文档

## 目录
- [概述](#概述)
- [公共说明](#公共说明)
- [认证接口](#认证接口)
- [用户管理接口](#用户管理接口)
- [测井数据接口](#测井数据接口)
- [钻井预测接口](#钻井预测接口)
- [文件上传接口](#文件上传接口)
- [报告模板接口](#报告模板接口)
- [报告生成接口](#报告生成接口)
- [待办事项接口](#待办事项接口)
- [通知管理接口](#通知管理接口)
- [AI对话接口](#ai对话接口)
- [系统监控接口](#系统监控接口)
- [统计分析接口](#统计分析接口)
- [数据导出接口](#数据导出接口)

---

## 概述

测井曲线智能重构系统是一个专业的测井数据分析平台，提供测井数据管理、钻井预测、报告生成、任务管理等功能。

### 基础信息
- **基础URL**: `http://localhost:8080`
- **通信协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Token

---

## 公共说明

### 响应格式
所有接口均返回统一的响应格式：
```json
{
  "code": 0,          // 状态码：0-成功，1-失败，500-服务器错误，404-未找到
  "message": "操作成功", // 响应消息
  "data": {}          // 响应数据（具体结构根据接口而定）
}
```

### 认证机制
除登录、注册接口外，其他接口都需要在请求头中携带JWT Token：
```
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 分页参数
分页查询接口通用参数：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 |

---

## 认证接口

### 用户注册
**POST** `/user/register`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| username | String | 是 | 用户名（4-16位字母数字下划线） |
| password | String | 是 | 密码（6-16位字母数字下划线） |

#### 请求示例
```json
{
  "username": "testuser",
  "password": "123456"
}
```

#### 响应示例
```json
{
  "code": 0,
  "message": "注册成功",
  "data": null
}
```

### 用户登录
**POST** `/user/login`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

#### 响应示例
```json
{
  "code": 0,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 用户管理接口

### 获取用户信息
**GET** `/user/userInfo`

#### 请求头
```
Authorization: JWT_TOKEN
```

#### 响应示例
```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "avatar": "http://example.com/avatar.jpg",
    "createTime": "2024-01-01T00:00:00"
  }
}
```

### 更新用户信息
**PUT** `/user/update`

#### 请求头
```
Authorization: JWT_TOKEN
```

#### 请求参数
```json
{
  "id": 1,
  "username": "updated_user",
  "email": "updated@example.com"
}
```

### 更新头像
**PATCH** `/user/updateAvatar`

#### 请求头
```
Authorization: JWT_TOKEN
```

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| avatarurl | String | 是 | 头像URL地址 |

### 修改密码
**PATCH** `/user/updatePwd`

#### 请求头
```
Authorization: JWT_TOKEN
```

#### 请求参数
```json
{
  "old_pwd": "old_password",
  "new_pwd": "new_password",
  "re_pwd": "new_password"
}
```

---

## 测井数据接口

### 获取测井数据列表
**GET** `/api/logdata/list`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| datasetName | String | 否 | 数据集名称 |
| wellName | String | 否 | 井名 |
| minDepth | BigDecimal | 否 | 最小深度 |
| maxDepth | BigDecimal | 否 | 最大深度 |
| isOriginal | Boolean | 否 | 是否原始数据 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页大小 |

#### 响应示例
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 1,
        "userId": 1,
        "datasetName": "测试数据集",
        "wellName": "测试井",
        "depth": 1000.5,
        "ac": 80.2,
        "cal": 220.5,
        "gr": 45.3,
        "den": 2.5,
        "rt": 15.6,
        "rxo": 8.9,
        "isOriginal": true,
        "confidence": 0.95,
        "createTime": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

### 获取数据集列表
**GET** `/api/logdata/datasets`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

### 获取曲线统计信息
**GET** `/api/logdata/curve-statistics`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| datasetName | String | 是 | 数据集名称 |
| curveType | String | 是 | 曲线类型(ac/gr/rt/rxo) |

### 获取深度变化数据
**GET** `/api/logdata/depth-variation`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| datasetName | String | 是 | 数据集名称 |
| wellName | String | 是 | 井名 |
| paramType | String | 是 | 参数类型(ac/gr/rt/rxo) |
| depthStart | BigDecimal | 是 | 起始深度 |
| depthEnd | BigDecimal | 是 | 结束深度 |

### 获取基础信息
**GET** `/api/logdata/basic-info`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| datasetName | String | 是 | 数据集名称 |

---

## 钻井预测接口

### 执行钻井预测
**POST** `/api/drilling/predict`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| datasetName | String | 是 | 数据集名称 |
| wellName | String | 是 | 井名 |
| depthRange | String | 是 | 深度范围 |
| requestData | Array | 是 | 5个时间步的测井参数数组 |

#### 请求体示例
```json
[
  {
    "data": {
      "parameters": {
        "AC": 80.2,
        "GR": 45.3,
        "RT": 15.6,
        "RXO": 8.9
      }
    }
  },
  {
    "data": {
      "parameters": {
        "AC": 81.5,
        "GR": 46.1,
        "RT": 16.2,
        "RXO": 9.1
      }
    }
  }
]
```

#### 响应示例
```json
{
  "code": 0,
  "message": "操作成功",
  "data": 12.5
}
```

### 查询预测历史
**GET** `/api/drilling/history`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| datasetName | String | 否 | 数据集名称 |
| wellName | String | 否 | 井名 |
| status | String | 否 | 状态 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页大小 |

### 导出预测历史CSV
**GET** `/api/export/history/csv`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

---

## 文件上传接口

### 上传文件
**POST** `/upload`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | MultipartFile | 是 | 上传的文件（最大10MB） |

#### 响应示例
```json
{
  "code": 0,
  "message": "上传成功",
  "data": "http://example.com/files/uploaded_file.jpg"
}
```

---

## 报告模板接口

### 获取模板列表
**GET** `/api/report/templates`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| keyword | String | 否 | 关键词搜索 |
| category | String | 否 | 分类 |
| isPublic | Boolean | 否 | 是否公开 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页大小 |

### 获取模板详情
**GET** `/api/report/templates/{id}`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

---

## 报告生成接口

### 生成报告预览
**POST** `/api/reports/generate-preview`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| templateId | Long | 是 | 模板ID |
| reportData | Object | 是 | 报告数据 |

#### 请求体示例
```json
{
  "userId": 1,
  "templateId": 1,
  "reportData": {
    "wellName": "测试井",
    "datasetName": "测试数据集",
    "predictionResult": 12.5,
    "confidence": 0.95
  }
}
```

### 生成最终报告
**POST** `/api/reports/generate-final`

#### 请求参数同上

### 获取报告历史
**GET** `/api/reports/history`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

### 获取报告详情
**GET** `/api/reports/{id}`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

---

## 待办事项接口

### 获取待办事项列表
**GET** `/todo/list`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| status | Integer | 否 | 状态(0-待完成,1-已完成,2-已取消) |
| priority | Integer | 否 | 优先级(0-低,1-中,2-高) |
| category | String | 否 | 分类 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页大小 |

### 创建待办事项
**POST** `/todo/create`

#### 请求参数
```json
{
  "userId": 1,
  "title": "完成数据分析",
  "content": "分析测井数据并生成报告",
  "priority": 2,
  "dueDate": "2024-12-31T23:59:59",
  "category": "数据分析"
}
```

### 更新待办事项
**PUT** `/todo/update`

#### 请求参数
```json
{
  "id": 1,
  "userId": 1,
  "title": "更新后的标题",
  "content": "更新后的内容",
  "priority": 1,
  "dueDate": "2024-12-31T23:59:59",
  "category": "更新分类"
}
```

### 更新待办状态
**PATCH** `/todo/status`

#### 请求参数
```json
{
  "id": 1,
  "userId": 1,
  "status": 1
}
```

### 删除待办事项
**DELETE** `/todo/delete/{id}`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

### 获取统计信息
**GET** `/todo/statistics`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

---

## 通知管理接口

### 获取通知列表
**GET** `/api/notifications`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |
| status | String | 否 | 状态 |
| type | String | 否 | 类型 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页大小 |

### 获取通知详情
**GET** `/api/notifications/{id}`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

### 标记已读
**PATCH** `/api/notifications/{id}/read`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

### 批量标记已读
**PATCH** `/api/notifications/batch-read`

#### 请求参数
```json
{
  "ids": [1, 2, 3],
  "userId": 1
}
```

### 删除通知
**DELETE** `/api/notifications/{id}`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

### 批量删除通知
**DELETE** `/api/notifications/batch-delete`

#### 请求参数
```json
{
  "ids": [1, 2, 3],
  "userId": 1
}
```

---

## AI对话接口

### 流式对话
**POST** `/api/chat/stream`

#### 请求头
```
Content-Type: application/json
Accept: text/event-stream
```

#### 请求参数
```json
{
  "model": "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B",
  "messages": [
    {
      "role": "user",
      "content": "请帮我分析这段测井数据"
    }
  ],
  "maxTokens": 2000,
  "temperature": 0.7,
  "enableThinking": true
}
```

#### 响应格式（SSE流式）
```
data: {"type":"thinking","content":"正在分析您的数据...","isDone":false}

data: {"type":"generation","content":"根据您提供的测井数据...","isDone":false}

data: {"type":"done","content":"对话结束","isDone":true}
```

---

## 系统监控接口

### 获取系统监控数据
**GET** `/api/monitor/system`

#### 响应示例
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "cpuUsage": 45.2,
    "memoryUsage": 68.5,
    "diskUsage": 32.1,
    "networkTraffic": 1024,
    "activeUsers": 15,
    "requestCount": 1250,
    "timestamp": "2024-01-01T00:00:00"
  }
}
```

---

## 统计分析接口

### 获取系统统计数据
**GET** `/api/statistics/system`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

#### 响应示例
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "totalWells": 25,
    "curvesCount": 12500,
    "avgAccuracy": 0.92,
    "processingTime": 120,
    "wellsGrowthRate": 15.5,
    "curvesAddedToday": 125,
    "accuracyLift": 8.2,
    "timeSavedRate": 65.3
  }
}
```

---

## 数据导出接口

### 导出测井数据CSV
**GET** `/api/export/logdata/csv`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | Integer | 是 | 用户ID |

#### 响应
返回CSV格式文件下载

---

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 0 | 成功 |
| 1 | 操作失败 |
| 401 | 未授权（Token无效或缺失） |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有需要认证的接口必须在请求头中包含有效的JWT Token
2. 文件上传大小限制为10MB
3. 分页查询最大页面大小为100条记录
4. 时间格式统一使用ISO 8601标准（YYYY-MM-DDTHH:mm:ss）
5. 深度单位为米（m），电阻率单位为欧姆·米（Ω·m）
6. 所有数值型参数支持小数点后2-4位精度

---