
#docker-compose up --build  // Nếu bạn đã chỉnh sửa các file mà cần đóng gói vào image (như code Java hoặc file cấu hình), bạn sẽ cần rebuild lại image. Chạy lệnh này giúp đảm bảo mọi thay đổi trong mã nguồn đều được cập nhật

# Stage 1: Build the JAR
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# Base image được sử dụng để build image
FROM --platform=amd64 openjdk:17.0.2-oraclelinux8

# Thông tin tác giả
LABEL authors="khanhvu"

# Set working directory trong container
WORKDIR /app

# Copy file JAR được build từ ứng dụng Spring Boot vào working directory trong container
COPY target/snapheal-0.0.1-SNAPSHOT.jar app.jar

# Expose port của ứng dụng
EXPOSE 8080

# Chỉ định command để chạy ứng dụng khi container khởi chạy
CMD ["java", "-jar", "app.jar"]
