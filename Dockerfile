#스프링 부트 프로젝트 빌드 및 jar 파일 생성
FROM openjdk:11-jdk-slim as builder
COPY gradlew .
# 실제 파일을 이미지에 복사한다 (CD에 파일을 옮기는 것과 같다.)
COPY gradle gradle
# 폴더를 옮길 때는 폴더 이름을 써야한다.
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootjar

# builder에서 jar 파일 복사 및 실행 -> base image를 줄이는 것이 효율적이다.
FROM openjdk:11-jre-slim
# heurit-refactoring-0.0.1-SNAPSHOT.jar 이라는 이름으로 현재 이미지에 저장한다.
COPY --from=builder build/libs/*.jar faramart.jar
VOLUME /tmp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/faramart.jar"]
