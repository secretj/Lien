#!/bin/bash

# MySQL 컨테이너가 실행 중인지 확인
if [ "$(docker ps -q -f name=lien-mysql)" ]; then
  echo "MySQL 컨테이너가 이미 실행 중입니다."
else
  echo "MySQL 컨테이너를 실행합니다..."
  docker compose up -d mysql
fi

# Spring Boot 앱 실행
./gradlew bootRun 