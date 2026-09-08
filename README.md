# ФестХаб — менеджер игровых фестивалей

Микросервисная платформа для организации игровых фестивалей: заявки компаний,
расписание событий, стенды, билеты с QR-кодами, награды и уведомления.

**Стек:** Java 17, Spring Boot 4, Spring Cloud OpenFeign, Spring Security + JWT,
Liquibase, PostgreSQL, Apache Kafka (KRaft), MinIO, Gradle · React 18,
TypeScript, Vite, TanStack Query, Zustand · Docker Compose.

## Быстрый старт

Нужен только Docker (Desktop или Engine + Compose v2). Всё остальное —
JDK, Gradle, Node — собирается внутри образов.

```bash
docker compose up --build -d
```

Первый запуск занимает несколько минут: Gradle и npm скачивают зависимости.
Последующие сборки используют кэш.

| Что | Адрес |
| --- | --- |
| Фронтенд | http://localhost:3000 |
| Консоль MinIO | http://localhost:9001 (`minioadmin` / `minioadmin`) |
| Kafka (с хоста) | `localhost:9092` |

### Секреты

Система поднимается без настройки — все ключи имеют dev-значения по умолчанию.
Но вход через Яндекс ID и отправка почты требуют своих учётных данных:

```bash
cp .env.example .env   # заполнить YANDEX_* и MAIL_*
```

`.env` в `.gitignore`. Compose подхватывает его автоматически, если файл есть.
Список переменных и где их взять — в [`.env.example`](.env.example).

Полезные команды:

```bash
docker compose ps                     # статус контейнеров
docker compose logs -f authservice    # логи одного сервиса
docker compose down                   # остановить
docker compose down -v                # остановить и удалить данные БД/MinIO
```

## Архитектура

Девять сервисов, у каждого своя база данных. Синхронные вызовы идут через
OpenFeign, асинхронные события — через Kafka. Фронтенд ходит только на
`/api/*`; nginx внутри контейнера `frontend` раскладывает эти пути по сервисам
(в dev-режиме ту же роль играет прокси Vite из `vite.config.ts`).

| Сервис | Порт | Назначение |
| --- | --- | --- |
| `applicationservice` | 8081 | заявки компаний и на участие в фестивалях |
| `authservice` | 8082 | регистрация, вход, JWT, OAuth2 (Яндекс) |
| `companyservice` | 8083 | компании-участники |
| `eventservice` | 8084 | события фестиваля, стенды, команды, награды |
| `festivalservice` | 8085 | фестивали, билеты, QR-коды |
| `fileservice` | 8086 | загрузка файлов, хранение в MinIO |
| `locationservice` | 8087 | города и площадки |
| `notificationservice` | 8088 | уведомления и почта |
| `userservice` | 8089 | профили пользователей |

Общий код вынесен в модули `common` (DTO, обработка ошибок, Feign-конфигурация)
и `jwt` (выпуск и проверка токенов).

Порты сервисов и инфраструктуры опубликованы на хосте, поэтому любой сервис
можно поднять из IDE против инфраструктуры в Docker — база, Kafka и MinIO
доступны по тем же адресам, что прописаны в `application.yaml`.

## Разработка без Docker

```bash
docker compose up -d $(docker compose config --services | grep -E 'postgres|kafka|minio')
./gradlew :authservice:bootRun          # нужный сервис
cd frontend && npm install && npm run dev   # http://localhost:3000
```

## Как устроена сборка

`Dockerfile` в корне — один для всех девяти сервисов. Стадия `builder`
одинакова для каждого из них, поэтому BuildKit собирает Gradle-проект один раз
и переиспользует слой; итоговый образ выбирается аргументом `SERVICE`:

```bash
docker build --build-arg SERVICE=authservice -t festivalmanager/authservice .
```

`frontend/Dockerfile` собирает бандл Vite и отдаёт его через nginx
(конфигурация — в `frontend/docker/`).
