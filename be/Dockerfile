# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom + src cùng lúc để tận dụng cache layer:
# khi pom không đổi thì lần build sau khỏi tải lại dependency.
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:resolve

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: run ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# wget dùng cho healthcheck, không cài thêm package nào.
RUN apk add --no-cache wget

RUN addgroup -S foodrush && adduser -S foodrush -G foodrush

# DataSeeder ghi file mật khẩu staff/admin khi sinh mật khẩu ngẫu nhiên.
# Phải tạo VÀ đổi chủ trước khi chuyển sang user không phải root,
# vì /app thuộc root nên user foodrush không tự tạo được thư mục con.
RUN mkdir -p /app/data && chown -R foodrush:foodrush /app

COPY --from=build /build/target/*.jar /app/app.jar

USER foodrush

EXPOSE 8080

ENV SEED_STAFF_PASSWORD_FILE=/app/data/staff-credentials.txt \
    ADMIN_PASSWORD_FILE=/app/data/admin-credentials.txt

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]