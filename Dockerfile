# 使用 Maven 镜像进行构建
FROM maven:3.9.8-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件
COPY backend/pom.xml .

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY backend/src ./src

# 构建应用
RUN mvn package -DskipTests

# 使用轻量级的 Java 镜像运行应用
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 Jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]