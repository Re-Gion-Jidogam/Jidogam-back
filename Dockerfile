# 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew ./
COPY gradle ./gradle/
COPY settings.gradle build.gradle ./

# 권한 부여
RUN chmod +x ./gradlew

# 의존성 캐싱
RUN ./gradlew dependencies --no-daemon || return 0

# 소스 복사 및 빌드
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# JVM 옵션
ENV JVM_OPTS="-Xmx512m -Xms256m -XX:MaxMetaspaceSize=256m"

# Spring Boot 기본 포트
EXPOSE 8080

# 필요한 디렉토리 생성
RUN mkdir -p /tmp/backup && chmod 777 /tmp/backup

# JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 실행 (타임존 + JVM 옵션)
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -Duser.timezone=Asia/Seoul -jar app.jar"]