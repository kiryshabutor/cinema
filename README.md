# MovieCat

MovieCat — REST API каталога фильмов на Spring Boot + PostgreSQL.

Проект включает:
- CRUD для `movies`, `directors`, `genres`, `studios`, `reviews`;
- расширенный поиск фильмов (JPQL и native SQL);
- in-memory индекс (кеш поиска) с инвалидацией;
- пагинацию для всех списковых `GET`-эндпоинтов.

## Технологии

- Java 17
- Spring Boot 3.4
- Spring Web
- Spring Data JPA (Hibernate)
- PostgreSQL 16
- Vue 3 + Vite (SPA client)
- Lombok
- Maven + Checkstyle
- Docker / Docker Compose

## Доменная модель

- `Movie` -> many-to-one -> `Director`
- `Movie` -> many-to-one -> `Studio`
- `Movie` -> many-to-many -> `Genre`
- `Movie` -> one-to-many -> `Review`

## Быстрый старт

### 1) Запуск в Docker (приложение + БД)

```bash
docker compose up --build
```

Сервисы:
- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432` (`moviecat` / `moviecat`)

### 2) Локальный запуск приложения (БД в Docker)

```bash
docker compose up -d postgres
mvn spring-boot:run
```

Для локального запуска используются переменные из `.env`:

```env
DB_USERNAME=moviecat
DB_PASSWORD=moviecat
```

## Frontend (Лаба 7)

SPA-клиент расположен в `frontend/` и работает с тем же API.

### Dev-режим (Vite + proxy)

```bash
cd frontend
npm install
npm run dev
```

Открыть:
- `http://localhost:5173/app/#/movies`

### Встроить SPA в Spring static (для лабы 8 / Docker)

```bash
cd frontend
npm run build:backend
```

Команда собирает `dist` и копирует его в:
- `src/main/resources/static/app`

После этого SPA отдается Spring-приложением по адресу:
- `http://localhost:8080/app/#/movies`

## Seed данных (1000 реальных фильмов + отзывы)

Скрипт полностью очищает и заново заполняет таблицы:
- `directors`
- `genres`
- `studios`
- `movies`
- `movie_genre`
- `reviews`

После запуска сид генерирует:
- `1000` фильмов
- `443` режиссёра
- `251` студию
- `13` жанров
- от `30` до `150` отзывов на каждый фильм
- реальные постеры из Wikipedia для большинства фильмов и SVG-фолбек для промахов в `uploads/seed-posters`

Основа сид-данных теперь собирается из реального IMDb-derived CSV через:
- `scripts/db/build_real_seed_sql.py`
- `scripts/db/fetch_seed_posters.py`

Запуск:

```bash
bash scripts/db/seed_medium.sh
```

По умолчанию:
- контейнер БД: `moviecat-db`
- пользователь: `moviecat`
- база: `moviecat_db`

Можно переопределить:
- `DB_CONTAINER`
- `DB_USER`
- `DB_NAME`

## Пагинация

Пагинация есть на всех списковых endpoint'ах:
- `GET /api/movies`
- `GET /api/movies/search`
- `GET /api/directors`
- `GET /api/genres`
- `GET /api/studios`
- `GET /api/reviews`
- `GET /api/reviews/movie/{movieId}`

Общие query-параметры:
- `page` (default `0`)
- `size` (default `10`, max `100`)
- `sort`
- `direction` (`asc`/`desc`)

## Advanced Search

Эндпоинт:

```http
GET /api/movies/search
```

Параметры фильтрации:
- `title`
- `directorLastName`
- `genreName`
- `studioTitle`
- `native` (`false` = JPQL, `true` = native SQL)

Пример:

```http
/api/movies/search?title=star&directorLastName=nolan&genreName=action&page=0&size=7&sort=title&direction=asc&native=false
```

Нормализация параметров:
- пустые строковые фильтры считаются "фильтр выключен";
- поиск case-insensitive;
- невалидный `sort` -> fallback на поле по умолчанию;
- невалидный `direction` -> `asc`;
- `page < 0` -> `0`;
- `size <= 0` -> `10`;
- `size > 100` -> `100`.

## In-Memory индекс (кеш поиска)

Кеш хранит результаты `GET /api/movies/search` в:
- `HashMap<MovieSearchKey, Page<MovieResponseDto>>`

Логи:
- `CACHE MISS` — ключ не найден, идем в БД
- `CACHE HIT` — ответ отдан из кеша
- `CACHE INVALIDATED` — кеш очищен после мутаций

Инвалидация movie-кеша:
- `Movie`: create/update/patch/delete/uploadPoster/importPoster -> полный сброс search/by-id кеша.
- `Director`/`Genre`/`Studio` `update` -> полный сброс только search-кеша + точечный `evict` by-id кеша для связанных фильмов.
- `Director`/`Genre`/`Studio` `create` и `delete` (когда нет связанных фильмов) -> movie-кеш не сбрасывается.

Как быстро проверить:

1. Выполни одинаковый `/api/movies/search` два раза подряд.
2. В логах будет сначала `CACHE MISS`, потом `CACHE HIT`.
3. Сделай `PUT/PATCH/DELETE` по фильму или `update` жанра/режиссера/студии.
4. В логах появится `CACHE INVALIDATED`.
5. Следующий такой же поиск снова даст `CACHE MISS`.

## Полезные команды

```bash
# компиляция
mvn -q -DskipTests compile

# checkstyle
mvn -q -DskipTests checkstyle:check

# логи контейнера приложения
docker logs -f moviecat-app
```

## Краткая карта API

### Movies
- `GET /api/movies` — список фильмов (Page)
- `GET /api/movies/search` — расширенный поиск (Page)
- `GET /api/movies/nplus1-demo` — демонстрация N+1
- `GET /api/movies/{id}`
- `POST /api/movies`
- `POST /api/movies/{movieId}/reviews/async` (асинхронный bulk отзывов)
- `POST /api/movies/{id}/views` (увеличить счетчик просмотров)
- `POST /api/movies/{id}/views/race-demo` (демо race condition: unsafe/safe)
- `PUT /api/movies/{id}`
- `PATCH /api/movies/{id}`
- `DELETE /api/movies/{id}`
- `POST /api/movies/{id}/poster`
- `POST /api/movies/{id}/poster/import` (импорт постера по TMDB posterPath)
- `POST /api/movies/{id}/poster/scrape` (поиск постера через Wikipedia API и сохранение в uploads)

### Posters
- `GET /api/posters/tmdb/search` (поиск постеров в TMDB)

Примечание:
- Для `POST /api/movies/{id}/poster/scrape` API-ключ не нужен.

### Directors
- `GET /api/directors` (Page)
- `POST /api/directors`
- `PUT /api/directors/{id}`
- `DELETE /api/directors/{id}`

### Genres
- `GET /api/genres` (Page)
- `POST /api/genres`
- `PUT /api/genres/{id}`
- `DELETE /api/genres/{id}`

### Studios
- `GET /api/studios` (Page)
- `POST /api/studios`
- `PUT /api/studios/{id}`
- `DELETE /api/studios/{id}`

### Reviews
- `GET /api/reviews` (Page)
- `GET /api/reviews/movie/{movieId}` (Page)
- `POST /api/reviews`

### Tasks
- `GET /api/tasks/{taskId}` (статус async-задачи)

## Демонстрация async bulk-операции отзывов

1. Создай тестовый фильм и запомни `movieId`.
2. Запусти задачу: `POST /api/movies/{movieId}/reviews/async?fail=false&startDelaySec=1&itemDelaySec=1`.
3. Получи `taskId` из ответа (`202 Accepted`).
4. Опрашивай `GET /api/tasks/{taskId}` и наблюдай переходы `CREATED -> RUNNING -> COMPLETED`.
5. Для проверки rollback запусти задачу с `fail=true` и убедись, что статус становится `FAILED`.

## Демонстрация race condition для viewCount

1. Выбери `movieId`.
2. Запусти unsafe: `POST /api/movies/{movieId}/views/race-demo?mode=unsafe&threads=50&incrementsPerThread=1000`.
3. Зафиксируй `lostUpdates` (обычно `> 0`).
4. Запусти safe: `POST /api/movies/{movieId}/views/race-demo?mode=safe&threads=50&incrementsPerThread=1000`.
5. Проверь, что `lostUpdates = 0`.

Примечание по реализации счетчика просмотров:
- `POST /api/movies/{id}/views` использует in-memory write-behind буфер.
- Flush в БД (`movies.view_count`) выполняется каждые `30` секунд или при накоплении `1000` pending просмотров по фильму.
- Ответ `viewCount` всегда считается как `DB + pending`.

Подробные примеры запросов/ответов: [api_endpoints.md](api_endpoints.md).
