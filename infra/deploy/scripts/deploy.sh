#!/bin/bash
set -e

echo "📁 앱 디렉토리 설정"
APP_DIR=/home/ubuntu/factseeker-backend-deploy
IMAGE=docker.io/jjjsukwoo/factseeker-backend:latest
mkdir -p $APP_DIR

echo "📄 .env 저장"
echo "$ENV_FILE" > $APP_DIR/.env

echo "📄 application.yml 저장"
echo "$APPLICATION_YML" > $APP_DIR/application.yml

echo "🐳 Docker Pull"
docker pull $IMAGE

echo "🧹 기존 컨테이너 정리"
docker stop factseeker-backend || true
docker rm factseeker-backend || true

echo "🚀 새 컨테이너 실행"
docker run -d \
  --name factseeker-backend \
  --env-file $APP_DIR/.env \
  -v $APP_DIR/application.yml:/app/config/application.yml \
  -e SPRING_PROFILES_ACTIVE=dev \
  -p 8080:8080 \
  $IMAGE

echo "✅ 배포 완료"