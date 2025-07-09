#!/bin/bash

# 로그 확인 스크립트
if [ "$1" = "app" ]; then
    docker-compose logs -f app
elif [ "$1" = "mysql" ]; then
    docker-compose logs -f mysql
elif [ "$1" = "redis" ]; then
    docker-compose logs -f redis
elif [ "$1" = "rabbitmq" ]; then
    docker-compose logs -f rabbitmq
else
    docker-compose logs -f
fi 