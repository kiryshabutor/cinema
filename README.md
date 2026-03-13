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

## Seed данных (100 реальных фильмов)

Скрипт полностью очищает и заново заполняет таблицы:
- `directors`
- `genres`
- `studios`
- `movies`
- `movie_genre`

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

Инвалидация происходит при изменениях:
- `Movie`: create/update/patch/delete/uploadPoster/createWithReviews*
- `Director`: create/update/delete
- `Genre`: create/update/delete
- `Studio`: create/update/delete

Как быстро проверить:

1. Выполни одинаковый `/api/movies/search` два раза подряд.
2. В логах будет сначала `CACHE MISS`, потом `CACHE HIT`.
3. Сделай `PUT/PATCH/DELETE` по фильму или update жанра/режиссера/студии.
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
- `POST /api/movies/with-reviews`
- `PUT /api/movies/{id}`
- `PATCH /api/movies/{id}`
- `DELETE /api/movies/{id}`
- `POST /api/movies/{id}/poster`

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

Подробные примеры запросов/ответов: [api_endpoints.md](api_endpoints.md).
