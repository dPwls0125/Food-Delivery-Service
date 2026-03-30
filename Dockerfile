# 1단계: 빌드 환경 구성
FROM eclipse-temurin:21-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드를 모두 컨테이너 안으로 복사
COPY . .

# Gradle 래퍼에 실행 권한 부여
RUN chmod +x ./gradlew

# 어떤 모듈을 빌드할지 외부에서 인자(Argument)로 받음
ARG MODULE_NAME

# 선택한 모듈의 빌드 실행 (테스트 제외)
RUN ./gradlew :${MODULE_NAME}:bootJar -x test

# 2단계: 실제 실행 환경 구성 (가벼운 JRE 환경)
FROM eclipse-temurin:21-jre

WORKDIR /app

# 1단계에서 빌드할 때 사용했던 모듈 이름 가져오기
ARG MODULE_NAME

# 1단계 컨테이너(builder)에서 생성된 .jar 파일만 통째로 복사해서 현재 환경의 app.jar로 이름 변경
COPY --from=builder /app/${MODULE_NAME}/build/libs/*SNAPSHOT.jar app.jar

# 스프링 부트 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
