# 🚀 Запуск PostgreSQL через Docker Compose

Инструкция по локальному запуску PostgreSQL с автоматической генерацией `.env` файла. Изначально сделал для Postgres только, потом и приложение включил в compose-файл.

---

## 📦 Требования

- Docker >= 20.x
- Docker Compose (встроенный `docker compose`)
- Bash (Linux / macOS / WSL для Windows)

Проверить установку:

```bash
docker --version
docker compose version
```

---

## 1️⃣ Клонирование репозитория

```bash
git clone <repo_url>
cd <project_folder>
```

---

## 2️⃣ Генерация `.env` файла

В проекте есть скрипт `generate-env.sh`, который создаёт безопасные случайные значения для:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

Сделайте скрипт исполняемым и запустите:

```bash
chmod +x generate-env.sh
./generate-env.sh
```

После выполнения появится файл `.env`:

```env
POSTGRES_DB=generated_db
POSTGRES_USER=generated_user
POSTGRES_PASSWORD=generated_password
```

> ⚠️ Убедитесь, что `.env` добавлен в `.gitignore`

---

## 3️⃣ Проверка `docker-compose.yml`

Пример конфигурации:

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:16
    container_name: postgres_db
    restart: unless-stopped
    env_file:
      - .env
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## 4️⃣ Запуск контейнера

```bash
docker compose up -d
```
Или если надо пересобрать приложение (есть кеширование, но первый раз прогрев долгий) 
```bash
docker compose up -d --build
```

Проверить статус:

```bash
docker compose ps
```

Посмотреть логи:

```bash
docker compose logs -f
```

---

## 5️⃣ Подключение к базе данных

Параметры подключения:

- Host: `localhost`
- Port: `5432`
- Database: значение из `.env`
- User: значение из `.env`
- Password: значение из `.env`

Пример подключения через `psql`:

```bash
psql -h localhost -U <POSTGRES_USER> -d <POSTGRES_DB>
```

---

## 6️⃣ Остановка контейнера

Остановить контейнер:

```bash
docker compose down
```

Удалить контейнер вместе с данными:

```bash
docker compose down -v
```

---

# ✅ Готово

Теперь PostgreSQL запущен и готов к использованию.