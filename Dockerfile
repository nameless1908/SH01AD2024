
#docker-compose up --build  // Nếu bạn đã chỉnh sửa các file mà cần đóng gói vào image (như code Java hoặc file cấu hình), bạn sẽ cần rebuild lại image. Chạy lệnh này giúp đảm bảo mọi thay đổi trong mã nguồn đều được cập nhật

# Stage 1: Build the JAR
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM --platform=amd64 openjdk:17.0.2-oraclelinux8
LABEL authors="khanhvu"
WORKDIR /app

# Copy file JAR từ stage trước vào stage hiện tại
COPY --from=build /app/target/snapheal-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

