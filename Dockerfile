# 1. Java 17 slim 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 임시 디렉토리 볼륨 설정
VOLUME /tmp

# 3. 빌드된 JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 4. JAR 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
