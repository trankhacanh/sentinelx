# ---- Stage 1: Build ----
# Dùng image Maven đầy đủ CHỈ để biên dịch -- image này không xuất hiện trong kết quả cuối cùng.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml trước, tải dependency riêng -- tận dụng Docker layer cache: nếu chỉ sửa code
# (không sửa pom.xml), bước "mvn dependency:go-offline" không cần chạy lại, build nhanh hơn
# đáng kể trong CI/CD.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
# JRE (không phải JDK) -- production không cần trình biên dịch javac, chỉ cần chạy .jar.
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user -- nếu container bị chiếm quyền, kẻ tấn công không có quyền root ngay trên container.
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

# Không bake bất kỳ biến môi trường nhạy cảm nào ở đây -- mọi config đọc qua ENV lúc container
# khởi động (docker run -e / ECS task definition), đúng nguyên tắc "config qua môi trường"
# đã áp dụng xuyên suốt dự án từ .env (Phase 1).
ENTRYPOINT ["java", "-jar", "app.jar"]