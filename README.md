# SmartExam 智能在线考试平台

基于 SpringBoot3 + Vue3 + MySQL + Ollama 的前后端分离智能在线考试系统，面向管理员、教师、学生三类角色，实现从组卷、考试、AI阅卷到成绩分析的全流程闭环。

## 项目文档
具体项目见报告书

完整的课程设计报告书见：[系统报告书.docx](docs/系统报告书.docx)

报告书包含需求分析、概要设计、详细设计、数据库设计、系统实现、系统测试等完整内容。

## 技术栈

**后端**
- SpringBoot 3.2.5
- MyBatis-Plus 3.5.6
- SpringSecurity
- OkHttp 4.12.0（对接 Ollama）
- MySQL 8.0
- JDK 17

**前端**
- Vue3 + Vite5
- Vue Router 4
- Pinia
- Element Plus
- ECharts 6
- Axios

**AI能力**
- 本地部署 Ollama + gemma3:4b 模型
- 实现 AI 自动出题与主观题智能阅卷

## 功能模块

- **用户与权限管理**：RBAC 角色权限控制，区分管理员、教师、学生三类角色
- **试题管理**：支持单选、多选、判断、填空、简答五种题型，支持 AI 辅助出题
- **考试管理**：考试创建、组卷、发布、状态管控全流程
- **在线考试**：限时作答、答案定时自动缓存、手动/超时自动交卷
- **智能阅卷**：客观题自动判分，主观题 AI 智能评分 + 教师人工复核
- **成绩管理**：多维度成绩统计，ECharts 可视化展示
- **用户组管理**：学习小组创建、成员管理、考试范围精细化管控

## 本地运行

**后端**
1. 安装 JDK17、Maven、MySQL8.0
2. 创建数据库 `smart_exam`，执行初始化 SQL 脚本
3. 修改 `application.yml` 中的数据库连接信息
4. 启动 Ollama 服务并拉取 gemma3:4b 模型
5. 运行 SpringBoot 启动类

**前端**
1. 安装 Node.js 18 LTS
2. 进入前端目录执行 `npm install`
3. 执行 `npm run dev` 启动开发服务器

## 作者

九天
