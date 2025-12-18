# 智慧书城 (Smart Book City)

一个现代化的在线图书商城系统，提供完整的图书购买、管理和统计分析功能。

## 📚 项目简介

智慧书城是一个基于前后端分离架构的在线图书商城系统，旨在为用户提供便捷的图书浏览、购买和阅读体验，同时为管理员提供强大的后台管理功能。

### 核心优势
- 🎨 现代化的UI设计，流畅的用户体验
- 🔒 完善的用户认证与授权机制
- 📊 丰富的统计分析功能
- 🛒 完整的购物车和订单流程
- 💳 内置钱包系统，支持充值和提现
- 📱 响应式设计，适配各种设备

## 🛠️ 技术栈

### 后端技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 后端开发语言 |
| Spring Boot | 3.x | 后端框架 |
| MyBatis | 3.x | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Spring Security | 6.x | 安全框架 |
| JWT | - | 身份认证 |

### 前端技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Element Plus | 2.x | UI组件库 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP客户端 |

## 📁 目录结构

```
smart-book-city/
├── backend/                # 后端代码
│   ├── src/                # 源代码
│   │   ├── main/           # 主代码
│   │   │   ├── java/       # Java代码
│   │   │   │   └── com/bookstore/  # 包结构
│   │   │   └── resources/  # 资源文件
│   │   └── test/           # 测试代码
│   ├── pom.xml             # Maven配置
│   └── application.properties  # 应用配置
├── frontend/               # 前端代码
│   ├── src/                # 源代码
│   │   ├── components/     # Vue组件
│   │   ├── router/         # 路由配置
│   │   ├── store/          # 状态管理
│   │   ├── views/          # 页面组件
│   │   └── main.ts         # 入口文件
│   ├── package.json        # 依赖配置
│   └── vite.config.ts      # Vite配置
├── init.sql                # 数据库初始化脚本
└── README.md               # 项目说明
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+
- npm 9+

### 安装与运行

#### 1. 数据库初始化

##### 方式一：使用命令行

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE bookstore;

# 使用数据库
USE bookstore;

# 执行初始化脚本
SOURCE init.sql;
```

##### 方式二：使用图形化工具

1. **Navicat Premium**：
   - 打开Navicat Premium，连接到MySQL服务器
   - 右键点击"数据库"，选择"新建数据库"
   - 数据库名填写为 `bookstore`，字符集选择 `utf8mb4`，排序规则选择 `utf8mb4_unicode_ci`
   - 点击"确定"创建数据库
   - 双击打开刚创建的 `bookstore` 数据库
   - 点击"导入向导"按钮，选择 `init.sql` 文件
   - 按照向导提示完成导入

2. **MySQL Workbench**：
   - 打开MySQL Workbench，连接到MySQL服务器
   - 在左侧导航栏中点击"Schemas"
   - 右键点击空白处，选择"Create Schema..."
   - Schema Name填写为 `bookstore`，字符集选择 `utf8mb4`，排序规则选择 `utf8mb4_unicode_ci`
   - 点击"Apply"创建数据库
   - 双击打开刚创建的 `bookstore` 数据库
   - 点击顶部菜单栏的"File -> Run SQL Script..."
   - 选择 `init.sql` 文件，点击"Run"执行脚本

3. **其他图形化工具**：
   - 连接到MySQL服务器
   - 创建名为 `bookstore` 的数据库
   - 执行 `init.sql` 文件中的SQL语句

数据库初始化完成后，将包含所有必要的数据表结构。

#### 2. 后端配置

编辑 `backend/src/main/resources/application.properties` 文件，配置数据库连接：

```properties
# 数据库连接配置
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### 3. 启动后端服务

##### 方式一：使用Maven命令

```bash
cd backend
# 编译并运行
mvn spring-boot:run
```

##### 方式二：使用IDE启动

1. **IntelliJ IDEA**：
   - 打开IntelliJ IDEA，选择 `File -> Open`，导入 `backend` 目录
   - 等待Maven依赖下载完成
   - 在项目结构中找到 `src/main/java/com/bookstore/BookstoreApplication.java`
   - 右键点击该文件，选择 `Run 'BookstoreApplication'`

2. **Eclipse**：
   - 打开Eclipse，选择 `File -> Import -> Maven -> Existing Maven Projects`
   - 选择 `backend` 目录作为根目录
   - 等待Maven依赖下载完成
   - 在项目结构中找到 `BookstoreApplication.java`
   - 右键点击该文件，选择 `Run As -> Java Application`

后端服务将在 `http://localhost:8080` 启动。

#### 4. 启动前端服务

##### 方式一：使用命令行

```bash
cd frontend
# 安装依赖
npm install
# 启动开发服务器
npm run dev
```

##### 方式二：使用IDE启动

1. **VS Code**：
   - 打开VS Code，选择 `File -> Open Folder`，导入 `frontend` 目录
   - 等待VS Code自动检测项目类型并安装必要的扩展
   - 打开终端（`Ctrl+`），执行 `npm install` 安装依赖
   - 依赖安装完成后，执行 `npm run dev` 启动开发服务器

2. **IntelliJ IDEA/WebStorm**：
   - 打开IDE，选择 `File -> Open`，导入 `frontend` 目录
   - 等待IDE自动检测项目类型
   - 打开终端，执行 `npm install` 安装依赖
   - 在 `package.json` 文件中，找到 `scripts` 部分的 `dev` 脚本
   - 右键点击 `dev` 脚本，选择 `Run 'npm run dev'`

前端服务将在 `http://localhost:3000` 启动。

## 📋 功能模块

### 用户模块
- ✅ 用户注册与登录
- ✅ 密码修改
- ✅ 用户信息管理
- ✅ 管理员登录

### 图书模块
- ✅ 图书列表展示
- ✅ 图书详情查看
- ✅ 图书分类筛选
- ✅ 图书搜索

### 购物车模块
- ✅ 加入购物车
- ✅ 购物车商品管理
- ✅ 结算功能

### 订单模块
- ✅ 订单创建
- ✅ 订单支付
- ✅ 订单列表
- ✅ 订单详情
- ✅ 管理员订单管理

### 钱包模块
- ✅ 钱包余额查询
- ✅ 充值功能
- ✅ 提现功能
- ✅ 交易记录

### 统计分析模块
- ✅ 销售统计
- ✅ 畅销书籍排行
- ✅ 分类销售比例
- ✅ 订单状态分布
- ✅ 用户阅读统计

## 🗄️ 数据库设计

### 主要表结构

- **users** - 用户表
- **books** - 图书表
- **orders** - 订单表
- **order_items** - 订单项表
- **cart_items** - 购物车表
- **wallet** - 钱包表
- **wallet_transaction** - 钱包交易表
- **admin** - 管理员表

详细数据库设计请查看 `init.sql` 文件。

## 🔧 开发规范

### 后端开发规范

1. **命名规范**
   - 类名：大驼峰命名法（如 `UserService`）
   - 方法名：小驼峰命名法（如 `getUserById`）
   - 变量名：小驼峰命名法（如 `userName`）
   - 常量名：全大写，下划线分隔（如 `MAX_PAGE_SIZE`）

2. **代码结构**
   - 控制器层（Controller）：处理HTTP请求
   - 服务层（Service）：实现业务逻辑
   - 数据访问层（Mapper）：与数据库交互
   - 实体层（Entity）：定义数据模型
   - 工具层（Util）：通用工具方法

3. **异常处理**
   - 使用全局异常处理器统一处理异常
   - 自定义业务异常类 `BusinessException`
   - 资源未找到异常 `ResourceNotFoundException`

### 前端开发规范

1. **命名规范**
   - 组件名：大驼峰命名法（如 `BookDetail`）
   - 文件名：使用短横线分隔（如 `book-detail.vue`）
   - 变量名：小驼峰命名法（如 `bookList`）

2. **代码结构**
   - 组件拆分：按功能模块化拆分组件
   - 状态管理：使用Pinia管理全局状态
   - 路由设计：合理规划路由结构
   - API请求：统一封装Axios请求

3. **代码风格**
   - 使用TypeScript确保类型安全
   - 遵循Vue 3 Composition API规范
   - 组件Props使用类型定义
   - 合理使用Element Plus组件

## 📈 项目特点

1. **前后端分离架构**
   - 前端：Vue 3 + TypeScript + Vite
   - 后端：Spring Boot + MyBatis
   - RESTful API设计

2. **安全性设计**
   - JWT认证机制
   - Spring Security权限控制
   - 密码加密存储
   - 防止SQL注入

3. **性能优化**
   - 数据库索引优化
   - 分页查询
   - 前端懒加载
   - 缓存机制

4. **用户体验**
   - 响应式设计
   - 流畅的动画效果
   - 直观的操作界面
   - 友好的错误提示


## 📊 项目统计

- ✅ 完成核心功能开发
- ✅ 代码质量检查
- ✅ 基本测试覆盖
- ✅ 文档完善

---

感谢使用智慧书城系统！🎉