# WellLogCurveSystem
基于vue3+spring boot+LSTM神经网络的测井重构曲线系统

## 效果展示



# 技术

## 前端 webfronted

- Vue3

- Vue Router

- pinia

- Element-plus UI

- Axios

- ECharts

- vite

- @vueup/vue-quill(文本编辑)

- jsPDF

  

## 后端框架

- Spring Boot 3.5.9 - 主要应用框架
- Java 17 - 编程语言版本

## 数据库相关

- MySQL - 关系型数据库
- MyBatis 3.0.5 - ORM框架
- Spring Data JPA - JPA持久层框架
- PageHelper 1.4.7 - MyBatis分页插件

## 安全认证

- JWT (jjwt 0.11.5) - JSON Web Token实现用户认证

## 文件存储

- 七牛云 Kodo SDK 7.15.0 - 对象存储服务
- Apache Commons IO 2.15.1 - 文件操作工具包

## 网络通信

- OkHttp 4.12.0 - HTTP客户端
- Spring WebFlux - 响应式Web框架
- Spring WebClient - 响应式HTTP客户端

## AI集成

- Python 3.x - 机器学习模型推理脚本
- TensorFlow/Keras - 深度学习模型加载和预测
- scikit-learn (joblib) - 数据预处理和模型序列化
- NumPy - 数值计算库

## 系统监控

- OSHI 6.4.0 - 系统和硬件信息获取



# 本项目实现的功能

## 📊 数据管理模块

### 1. **数据上传** (`data-upload`)

- 支持多种测井数据格式上传
- 文件验证与预处理
- 批量导入功能

### 2. **曲线重建** (`curve-reconstruction`)

- **批量上传** - 批量曲线数据导入
- **手动输入** - 逐条输入测井参数
- **数据查看** - 曲线数据可视化展示

### 3. **历史数据管理** (`historical-data`)

- **历史列表** - 查看历史测井数据记录
- **导入导出** - 数据备份与恢复，支持多格式导出

------

## 📈 数据分析与可视化

### 4. **仪表板** (`dashboard`)

- 系统概览统计
- 关键指标展示
- 数据趋势分析图表

### 5. **报告系统** (`report`)

- **报告生成** - 自动生成孔隙度预测报告
- **报告模板** - 可配置的多种报告模板
- **可视化分析** - 交互式图表与数据分析

### 6. **报告生成工具** (`report-generation`)

- PDF导出功能
- HTML2Canvas截图
- 自定义报告内容

------

## 🤖 AI功能模块

### 7. **AI智能助手** (`AiAssistant.vue`)

- 自然语言对话
- 测井数据智能解读
- 预测结果分析建议

------

## 👤 用户管理

### 8. **用户认证** (`login`)

- 登录/注册功能
- 账户安全管理

### 9. **用户中心** (`user`)

- **用户资料** - 个人信息管理
- **密码修改** - 安全密码更新
- **用户头像** - 头像上传与管理

### 10. **用户统计** (userStatistics API)

- 用户活动统计
- 使用数据分析

------

## 🔧 系统功能

### 11. **系统监控** (`SystemMonitorChart.vue`, systemMonitor API)

- 系统性能监控
- 实时状态检查
- 资源使用情况展示

### 12. **通知系统** (`NotificationPage.vue`, notification API)

- 实时消息通知
- 事件提醒

### 13. **待办事项** (todo API)

- 任务管理
- 日程安排

------

## 🔌 技术实现特色

| 功能             | 实现技术             |
| ---------------- | -------------------- |
| **曲线可视化**   | ECharts 6.0          |
| **深度学习预测** | CNN-LSTM模型（后端） |
| **报告导出**     | jsPDF + html2canvas  |
| **实时通信**     | API 集成             |
| **状态管理**     | Pinia + Vuex模式     |
| **样式系统**     | Sass + Element Plus  |

------

## 📱 核心业务流程

数据上传 → 曲线重建 → 数据分析 → AI预测 → 报告生成 → PDF导出
   ↓
用户管理 + 系统监控 + 消息通知
