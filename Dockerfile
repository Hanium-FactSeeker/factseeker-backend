#Dockerfile
# 도커 이미지 지정
FROM openjdk:21-jdk-slim

# 빌드된 파일의 위치를 argument로 지정
ARG JAR_FILE=build/libs/factseeker-backend-0.0.1-SNAPSHOT.jar

# 위의 경로의 파일을 이미지 내부의 app.jar로 복사
COPY ${JAR_FILE} app.jar

# 컨테이너 시작시 명령어. 즉 해당 jar파일을 실행하겠다는 것
ENTRYPOINT [ "java", "-jar",  "/app.jar" ]