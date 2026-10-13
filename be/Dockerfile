# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom trước để tận dụng cache layer của Maven.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: run ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# wget dùng cho healthcheck, không cài thêm package nào.
RUN apk add --no-cache wget

# Chạy bằng user không phải root.
RUN addgroup -S foodrush && adduser -S foodrush -G foodrush
USER foodrush

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# File mật khẩu staff sinh ra lúc seed cần chỗ ghi được.
RUN mkdir -p /app/data && chown -R foodrush:foodrush /app
ENV SEED_STAFF_PASSWORD_FILE=/app/data/staff-credentials.txt

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]