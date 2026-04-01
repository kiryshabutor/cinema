# API Documentation

Все запросы идут в Spring Boot API.

- Base URL: `/` (обычно `http://localhost:8080`)
- Формат тела запроса/ответа: `application/json` (кроме загрузки постера)

## Общие ошибки

- `400 Bad Request`
  Причины: ошибки валидации DTO, некорректные параметры, пустой/невалидный файл.
- `404 Not Found`
  Причины: сущность по id не найдена.
- `409 Conflict`
  Причины: конфликт уникальности, попытка удалить сущность, на которую есть ссылки.
- `500 Internal Server Error`
  Причины: неперехваченные runtime-ошибки (например, `fail=true` в bulk-операции отзывов).

---

## Movies

### 1) Список фильмов

`GET /api/movies`

- Параметры пагинации и сортировки:
  - `page` (default: `0`)
  - `size` (default: `10`, max: `100`)
  - `sort` (default: `title`) — `title`, `year`, `viewCount`, `id`
  - `direction` (default: `asc`) — `asc` или `desc`
  - `native` (default: `false`) — `true` для native SQL, `false` для JPQL
- Фильтры не применяются (это полный список с пагинацией).

**Response (200 OK):** `Page<MovieResponseDto>`

### 2) Демонстрация N+1

`GET /api/movies/nplus1-demo`

- Намеренно использует обычный `findAll()` с LAZY-связями, чтобы воспроизвести N+1.

**Response (200 OK):** `List<MovieResponseDto>`

### 3) Фильм по id

`GET /api/movies/{id}`

**Response (200 OK):** `MovieResponseDto`

### 4) Расширенный поиск фильмов

`GET /api/movies/search`

Параметры:

- `title` (optional) — фильтр по названию, частичное совпадение, case-insensitive.
- `directorLastName` (optional) — фильтр по фамилии режиссера (связанная сущность `Director`).
- `genreName` (optional) — фильтр по названию жанра (связанная сущность `Genre`).
- `studioTitle` (optional) — фильтр по названию студии (связанная сущность `Studio`).
- `page` (default: `0`) — номер страницы.
- `size` (default: `10`, max: `100`) — размер страницы.
- `sort` (default: `title`) — доступно: `title`, `year`, `viewCount`, `id`.
- `direction` (default: `asc`) — `asc` или `desc`.
- `native` (default: `false`) — `true` для native SQL, `false` для JPQL.

Параметры с мягким fallback:

- невалидный `sort` -> `title`;
- невалидный `direction` -> `asc`;
- `page < 0` -> `0`;
- `size <= 0` -> `10`;
- `size > 100` -> `100`.

Пример:

`GET /api/movies/search?title=star&directorLastName=nolan&genreName=sci&page=0&size=5&sort=viewCount&direction=desc&native=true`

**Response (200 OK):** `Page<MovieResponseDto>`

Примечание:
- для `GET /api/movies/search` поле `genres` возвращается как пустой массив `[]`;
- фильтр `genreName` при этом работает.

Пример структуры ответа:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Interstellar",
      "year": 2014,
      "duration": 169,
      "viewCount": 100,
      "posterUrl": "/uploads/abc123.jpg",
      "directorId": 1,
      "directorLastName": "Nolan",
      "directorFirstName": "Christopher",
      "directorMiddleName": "Edward",
      "studioId": 1,
      "studioTitle": "Warner Bros",
      "genres": []
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5
  },
  "totalElements": 12,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 1
}
```

### 5) Создать фильм

`POST /api/movies`

**Request (MovieCreateDto):**

```json
{
  "title": "Interstellar",
  "year": 2014,
  "duration": 169,
  "viewCount": 0,
  "directorId": 1,
  "studioId": 1,
  "genreIds": [1, 9]
}
```

**Response (201 Created):** `MovieResponseDto`

### 6) Запустить async bulk создание отзывов для фильма

`POST /api/movies/{movieId}/reviews/async?fail=false&startDelaySec=0&itemDelaySec=0`

- `movieId` берется из path.
- `fail=true`: симуляция ошибки на последнем отзыве.
- `startDelaySec`: задержка в секундах перед переходом `CREATED -> RUNNING`.
- `itemDelaySec`: задержка в секундах перед обработкой каждого элемента.
- Операция выполняется асинхронно и всегда транзакционно.

**Request:** `List<ReviewCreateItemDto>`

Пример:

```json
[
  { "authorAlias": "user1", "rating": 9, "comment": "Great" },
  { "authorAlias": "user2", "rating": 8, "comment": "Good" }
]
```

**Response (202 Accepted):**

```json
{
  "taskId": 1,
  "status": "CREATED"
}
```

### 7) Увеличить счетчик просмотров фильма

`POST /api/movies/{id}/views`

- Увеличивает `viewCount` на `+1`.
- Инкремент сначала попадает в in-memory pending buffer (write-behind).
- Возвращаемое `viewCount` считается как `persisted(DB) + pending(in-memory)`.
- Flush pending в БД выполняется:
  - по времени (каждые `30` секунд),
  - или при достижении порога `1000` просмотров по фильму.

**Response (200 OK):**

```json
{
  "movieId": 113,
  "viewCount": 101
}
```

### 8) Запустить race demo для счетчика просмотров

`POST /api/movies/{id}/views/race-demo?mode=unsafe&threads=50&incrementsPerThread=1000`

- `mode`: `unsafe` или `safe`.
- `threads`: минимум `50`.
- `incrementsPerThread`: минимум `1`.
- Сценарий выполняется в памяти и после завершения добавляет `actualCount` в pending buffer фильма.

**Response (200 OK):**

```json
{
  "movieId": 113,
  "mode": "unsafe",
  "threads": 50,
  "incrementsPerThread": 1000,
  "expectedCount": 50000,
  "actualCount": 49720,
  "lostUpdates": 280,
  "durationMs": 141
}
```

Интерпретация:
- `expectedCount` — сколько инкрементов должно было выполниться.
- `actualCount` — сколько инкрементов реально применилось в памяти.
- `lostUpdates` — потерянные обновления (в `safe` режиме должно быть `0`).

### 9) Полное обновление фильма

`PUT /api/movies/{id}`

**Request (MovieUpdateDto):**

```json
{
  "title": "Interstellar Updated",
  "year": 2014,
  "duration": 170,
  "viewCount": 100,
  "directorId": 1,
  "studioId": 1,
  "genreIds": [2, 9]
}
```

**Response (200 OK):** `MovieResponseDto`

Фактическое поведение `PUT`:

- если `directorId = null`, режиссер у фильма очищается;
- если `studioId = null`, студия очищается;
- если `genreIds = null`, список жанров очищается.

### 10) Частичное обновление фильма

`PATCH /api/movies/{id}`

**Request (MoviePatchDto):**

```json
{
  "title": "Новое название",
  "year": 2025,
  "duration": 150,
  "viewCount": 200,
  "directorId": 2,
  "studioId": 3,
  "genreIds": [1, 4]
}
```

**Response (200 OK):** `MovieResponseDto`

Фактическое поведение `PATCH`:

- обновляются только переданные поля;
- `directorId`, `studioId`, `genreIds` меняются только если поле пришло с ненулевым значением;
- очистка связей через `null` в `PATCH` не реализована.

### 11) Удалить фильм

`DELETE /api/movies/{id}`

**Response (204 No Content)**

### 12) Загрузить постер

`POST /api/movies/{id}/poster`

**Request:** `multipart/form-data`

- `file`: изображение

Допустимые расширения:

- `.jpg`
- `.jpeg`
- `.png`
- `.webp`
- `.gif`

**Response (200 OK):**

```json
{ "url": "/uploads/<uuid>.<ext>" }
```

Статическая раздача: `/uploads/**`.

### MovieResponseDto (пример)

```json
{
  "id": 1,
  "title": "Interstellar",
  "year": 2014,
  "duration": 169,
  "viewCount": 100,
  "posterUrl": "/uploads/abc123.jpg",
  "directorId": 1,
  "directorLastName": "Nolan",
  "directorFirstName": "Christopher",
  "directorMiddleName": "Edward",
  "studioId": 1,
  "studioTitle": "Warner Bros",
  "genres": [
    { "id": 2, "name": "Drama" },
    { "id": 9, "name": "Sci-Fi" }
  ]
}
```

---

## Directors

### 1) Список режиссеров

`GET /api/directors`

**Response (200 OK):** `List<DirectorDto>`

### 2) Создать режиссера

`POST /api/directors`

```json
{
  "lastName": "Nolan",
  "firstName": "Christopher",
  "middleName": "Edward"
}
```

**Response (201 Created):** `DirectorDto`

### 3) Обновить режиссера

`PUT /api/directors/{id}`

```json
{
  "lastName": "Nolan",
  "firstName": "Christopher",
  "middleName": null
}
```

**Response (200 OK):** `DirectorDto`

### 4) Удалить режиссера

`DELETE /api/directors/{id}`

**Response (204 No Content)**

Дополнительно:

- уникальность проверяется по комбинации `lastName + firstName + middleName`;
- перед сохранением ФИО триммится;
- пустой `middleName` нормализуется в `null`;
- нельзя удалить режиссера, если есть фильмы (`409 Conflict`).

---

## Genres

### 1) Список жанров

`GET /api/genres`

**Response (200 OK):** `List<GenreDto>`

### 2) Создать жанр

`POST /api/genres`

```json
{ "name": "Sci-Fi" }
```

**Response (201 Created):** `GenreDto`

### 3) Обновить жанр

`PUT /api/genres/{id}`

```json
{ "name": "Sci-Fi Updated" }
```

**Response (200 OK):** `GenreDto`

### 4) Удалить жанр

`DELETE /api/genres/{id}`

**Response (204 No Content)**

Дополнительно:

- `name` должен быть уникальным;
- нельзя удалить жанр, если он используется в фильмах (`409 Conflict`).

---

## Studios

### 1) Список студий

`GET /api/studios`

**Response (200 OK):** `List<StudioDto>`

### 2) Создать студию

`POST /api/studios`

```json
{
  "title": "Warner Bros",
  "address": "Burbank, CA"
}
```

**Response (201 Created):** `StudioDto`

### 3) Обновить студию

`PUT /api/studios/{id}`

```json
{
  "title": "Warner Bros Updated",
  "address": "Hollywood, CA"
}
```

**Response (200 OK):** `StudioDto`

### 4) Удалить студию

`DELETE /api/studios/{id}`

**Response (204 No Content)**

Дополнительно:

- `title` должен быть уникальным;
- нельзя удалить студию, если на нее ссылаются фильмы (`409 Conflict`).

---

## Reviews

### 1) Все отзывы

`GET /api/reviews`

**Response (200 OK):** `List<ReviewDto>`

### 2) Отзывы по фильму

`GET /api/reviews/movie/{movieId}`

**Response (200 OK):** `List<ReviewDto>`

### 3) Создать отзыв

`POST /api/reviews`

**Request (ReviewDto):**

```json
{
  "authorAlias": "Critic1",
  "rating": 8,
  "comment": "Nice movie!",
  "movieId": 1
}
```

**Response (201 Created):** `ReviewDto`

Дополнительно:

- `movieId` обязателен и должен существовать в БД (`404`, если фильм не найден).

### 4) Запустить async bulk создание отзывов по фильму

`POST /api/movies/{movieId}/reviews/async?fail=false&startDelaySec=0&itemDelaySec=0`

- Тело запроса: `List<ReviewCreateItemDto>`
- `movieId` берется из path.
- `fail=true`: симуляция ошибки на последнем элементе.
- По `taskId` можно отслеживать выполнение через `GET /api/tasks/{taskId}`.
- Полный пример запроса см. в разделе Movies, пункт 6.

---

## Tasks

### 1) Получить статус async-задачи

`GET /api/tasks/{taskId}`

**Response (200 OK):**

```json
{
  "taskId": 1,
  "status": "RUNNING",
  "createdAt": "2026-03-30T19:00:00",
  "startedAt": "2026-03-30T19:00:02",
  "finishedAt": null,
  "totalCount": 2,
  "processedCount": 1,
  "errorMessage": null
}
```

---

## DTO и валидация (фактические ограничения)

### MovieCreateDto

- `title`: required, not blank
- `year`: required, `>= 1888`
- `duration`: required, `>= 1`
- `viewCount`: required, `>= 0`
- `directorId`, `studioId`: optional
- `genreIds`: optional

### MovieUpdateDto

- `title`: required, not blank
- `year`: required, `>= 1888`, `<= 2027`
- `duration`: required, `>= 1`
- `viewCount`: required, `>= 0`
- `directorId`, `studioId`, `genreIds`: optional

### MoviePatchDto

- все поля опциональны
- `year`: `>= 1888`, `<= 2027`
- `duration`: `>= 1`
- `viewCount`: `>= 0`
- `directorId`, `studioId`, `genreIds`: optional

### MovieResponseDto

- `id`, `title`, `year`, `duration`, `viewCount`, `posterUrl`
- `directorId`, `directorLastName`, `directorFirstName`, `directorMiddleName`
- `studioId`, `studioTitle`
- `genres`: `List<GenreItemDto>`

### GenreItemDto

- `id`: `Long`
- `name`: `String`

### ReviewDto

- `authorAlias`: required, not blank
- `rating`: required, `1..10`
- `comment`: optional
- `movieId`: required

### ReviewCreateItemDto

- `authorAlias`: required, not blank
- `rating`: required, `1..10`
- `comment`: optional

### DirectorDto

- `lastName`: required, not blank
- `firstName`: required, not blank
- `middleName`: optional

### GenreDto

- `name`: required, not blank

### StudioDto

- `title`: required, not blank
- `address`: optional
