#!/usr/bin/env bash

set -e

# Генерация случайных значений
POSTGRES_USER="user_$(openssl rand -hex 6)"
POSTGRES_PASSWORD="$(openssl rand -base64 24 | tr -dc 'A-Za-z0-9' | head -c 24)"
POSTGRES_DB="db_$(openssl rand -hex 4)"

# Создание .env файла
cat > .env <<EOF
POSTGRES_DB=${POSTGRES_DB}
POSTGRES_USER=${POSTGRES_USER}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
EOF

echo ".env файл успешно создан!"
echo "POSTGRES_DB=${POSTGRES_DB}"
echo "POSTGRES_USER=${POSTGRES_USER}"
echo "POSTGRES_PASSWORD=${POSTGRES_PASSWORD}"