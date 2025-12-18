# 智慧书城管理系统 - 后端

## 项目介绍
智慧书城管理系统是一个基于Spring Boot开发的在线图书销售平台后端，提供完整的图书管理、用户管理、购物车、订单管理、钱包管理和统计功能。

## 技术栈

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.0 | 后端框架 |
| MyBatis | 3.0.3 | ORM框架 |
| MySQL | 8.0.33 | 数据库 |
| Spring Security | 3.2.0 | 安全框架 |
| JWT | 0.12.3 | 认证授权 |
| Lombok | 1.18.30 | 代码简化工具 |
| EhCache | 2.10.9.2 | 缓存框架 |

## 功能模块

### 1. 图书管理
- 图书CRUD操作
- 图书分类查询
- 图书搜索（按标题、作者）
- 图书分页查询
- 批量删除图书

### 2. 用户管理
- 用户注册
- 用户登录（JWT认证）
- 用户信息查询
- 用户余额管理

### 3. 购物车功能
- 添加商品到购物车
- 修改购物车商品数量
- 从购物车删除商品
- 清空购物车

### 4. 订单管理
- 创建订单
- 订单支付
- 订单查询
- 订单状态管理

### 5. 钱包管理
- 钱包充值
- 订单支付
- 交易记录查询
- 余额查询

### 6. 统计功能
- 阅读统计
- 交易统计
- 用户行为统计

### 7. 管理员功能
- 图书管理
- 用户管理
- 订单管理

## 快速开始

### 1. 环境准备
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
1. 创建数据库：`CREATE DATABASE bookstore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. 修改`application.properties`中的数据库配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
```

### 3. 运行项目

#### 方式一：Maven命令
```bash
cd backend
mvn spring-boot:run
```

#### 方式二：IDE运行
直接运行`BookstoreApplication.java`类

### 4. 访问项目
项目启动后，访问：`http://localhost:8080`

## 项目结构

```
backend/src/main/java/com/bookstore/
├── BookstoreApplication.java          # 应用入口
├── common/                           # 公共模块
│   └── ApiResponse.java              # 统一响应格式
├── config/                           # 配置类
│   ├── JwtAuthenticationFilter.java  # JWT认证过滤器
│   ├── SecurityConfig.java           # Spring Security配置
│   └── WebConfig.java                # Web配置
├── controller/                       # 控制器
│   ├── AdminController.java          # 管理员控制器
│   ├── BookController.java           # 图书控制器
│   ├── CartController.java           # 购物车控制器
│   ├── OrderController.java          # 订单控制器
│   ├── ReadingStatsController.java   # 阅读统计控制器
│   ├── StatisticsController.java     # 统计控制器
│   ├── TransactionController.java    # 交易控制器
│   ├── UserController.java           # 用户控制器
│   └── WalletController.java         # 钱包控制器
├── dto/                              # 数据传输对象
│   ├── BookDTO.java                  # 图书DTO
│   ├── OrderDTO.java                 # 订单DTO
│   ├── WalletDTO.java                # 钱包DTO
│   └── WalletTransactionDTO.java     # 钱包交易DTO
├── entity/                           # 实体类
│   ├── Admin.java                    # 管理员实体
│   ├── Book.java                     # 图书实体
│   ├── CartItem.java                 # 购物车项实体
│   ├── Order.java                    # 订单实体
│   ├── OrderItem.java                # 订单项实体
│   ├── User.java                     # 用户实体
│   ├── Wallet.java                   # 钱包实体
│   └── WalletTransaction.java        # 钱包交易实体
├── exception/                        # 异常处理
│   ├── BusinessException.java        # 业务异常
│   ├── GlobalExceptionHandler.java   # 全局异常处理器
│   └── ResourceNotFoundException.java # 资源未找到异常
├── mapper/                           # Mapper接口
│   ├── AdminMapper.java              # 管理员Mapper
│   ├── BookMapper.java               # 图书Mapper
│   ├── CartItemMapper.java           # 购物车项Mapper
│   ├── OrderItemMapper.java          # 订单项Mapper
│   ├── OrderMapper.java              # 订单Mapper
│   ├── UserMapper.java               # 用户Mapper
│   ├── WalletMapper.java             # 钱包Mapper
│   └── WalletTransactionMapper.java  # 钱包交易Mapper
├── service/                          # 服务层
│   ├── AdminService.java             # 管理员服务
│   ├── BookService.java              # 图书服务
│   ├── CartService.java              # 购物车服务
│   ├── OrderService.java             # 订单服务
│   ├── ReadingStatsService.java      # 阅读统计服务
│   ├── StatisticsService.java        # 统计服务
│   ├── TransactionService.java       # 交易服务
│   ├── UserService.java              # 用户服务
│   ├── WalletService.java            # 钱包服务
│   └── impl/                         # 服务实现
│       └── UserDetailsServiceImpl.java # Spring Security用户详情服务
└── util/                             # 工具类
    ├── BookDtoMapper.java            # 图书DTO转换
    └── JwtUtils.java                 # JWT工具类
```

## 数据库初始化

### 1. 创建数据库和表结构

**方式一：手动执行SQL脚本**
1. 首先执行`bookstore_init.sql`脚本创建数据库和表结构：
   ```bash
   mysql -u root -p < src/main/resources/bookstore_init.sql
   ```

2. 然后执行`data.sql`脚本初始化数据：
   ```bash
   mysql -u root -p bookstore < src/main/resources/data.sql
   ```

**方式二：在MySQL客户端中执行**
1. 登录MySQL客户端：
   ```bash
   mysql -u root -p
   ```

2. 执行以下命令：
   ```sql
   CREATE DATABASE IF NOT EXISTS bookstore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   USE bookstore;
   SOURCE src/main/resources/bookstore_init.sql;
   SOURCE src/main/resources/data.sql;
   ```

### 2. 初始化数据内容
执行`data.sql`脚本后，会初始化以下数据：
- 管理员账号：admin/123456
- 普通用户：user/123456
- 各类图书数据（小说、科技、教育、经济等）

## API文档

### 认证方式
所有需要认证的API都需要在请求头中携带JWT令牌：
```
Authorization: Bearer <token>
```

### 主要API端点

#### 1. 图书相关
- `GET /api/books` - 获取所有图书
- `GET /api/books/paged` - 分页获取图书
- `GET /api/books/{id}` - 根据ID获取图书
- `GET /api/books/search` - 搜索图书
- `GET /api/books/categories` - 获取所有分类
- `POST /api/books` - 创建图书
- `PUT /api/books/{id}` - 更新图书
- `DELETE /api/books/{id}` - 删除图书

#### 2. 用户相关
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/{id}` - 获取用户信息

#### 3. 购物车相关
- `GET /api/cart/user/{userId}` - 获取用户购物车
- `POST /api/cart` - 添加商品到购物车
- `PUT /api/cart/{id}` - 更新购物车商品数量
- `DELETE /api/cart/{id}` - 从购物车删除商品

#### 4. 订单相关
- `GET /api/orders/user/{userId}` - 获取用户订单
- `POST /api/orders` - 创建订单
- `GET /api/orders/{id}` - 获取订单详情
- `PUT /api/orders/{id}/pay` - 支付订单

#### 5. 钱包相关
- `GET /api/wallet/user/{userId}` - 获取用户钱包
- `POST /api/wallet/recharge` - 钱包充值
- `GET /api/wallet/transactions/{userId}` - 获取交易记录

## 开发规范

### 1. 代码命名规范
- 类名：采用大驼峰命名法，如`BookController`
- 方法名：采用小驼峰命名法，如`getBookById`
- 变量名：采用小驼峰命名法，如`bookId`
- 常量名：采用全大写，下划线分隔，如`MAX_PAGE_SIZE`

### 2. 注释规范
- 类注释：说明类的功能和作用
- 方法注释：说明方法的功能、参数、返回值和异常
- 字段注释：说明字段的含义

### 3. 异常处理
- 业务异常：使用`BusinessException`，并指定错误码和错误信息
- 全局异常：由`GlobalExceptionHandler`统一处理
- 异常信息：清晰明确，便于前端展示

## 部署说明

### 1. 编译打包
```bash
cd backend
mvn clean package -DskipTests
```

### 2. 运行jar包
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### 3. Docker部署（可选）
```bash
# 构建镜像
docker build -t smart-book-city-backend .

# 运行容器
docker run -d -p 8080:8080 --name smart-book-city-backend smart-book-city-backend
```

## 开发工具

- IDE：IntelliJ IDEA 或 Eclipse
- 数据库管理工具：Navicat 或 DBeaver
- API测试工具：Postman 或 Insomnia

## 贡献指南

1. Fork本仓库
2. 创建特性分支：`git checkout -b feature/xxx`
3. 提交代码：`git commit -m 'Add feature: xxx'`
4. 推送到分支：`git push origin feature/xxx`
5. 提交Pull Request

## 版本历史

- v1.0.0：初始版本，包含完整的图书管理、用户管理、购物车、订单管理和钱包管理功能

## 许可证

MIT License

## 联系方式

如有问题或建议，请联系：
- 邮箱：admin@example.com
- 项目地址：https://github.com/yourname/smart-book-city
