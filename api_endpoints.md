# API Documentation

Все запросы к API проходят через **Spring Boot Service**.
Base URL: `/` (обычно `http://localhost:8080`)

## Общие ответы ошибок

- `400 Bad Request`: ошибки валидации входных данных.
- `404 Not Found`: ресурс не найден.
- `409 Conflict`: конфликт уникальности (например, фильм с таким названием уже существует).

## Movies

### Список фильмов (с демонстрацией N+1)
`GET /api/movies?fetchType={type}`

- `fetchType=eager` (default): оптимизированный запрос (JOIN FETCH).
- `fetchType=lazy`: стандартный `findAll()` (демонстрация N+1).

**Response (200 OK):** `List<MovieDto>`

### Поиск фильмов по названию
`GET /api/movies/search?title={title}`

**Response (200 OK):** `List<MovieDto>`

### Получить фильм по ID
`GET /api/movies/{id}`

**Response (200 OK):** `MovieDto`

### Создать фильм
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
  "genreIds": [1],
  "reviews": [
    {"authorAlias": "User1", "rating": 10, "comment": "Great!", "movieId": 1}
  ]
}
```

**Response (201 Created):** `MovieDto`

### Создать фильм с отзывами
`POST /api/movies/with-reviews?fail=false&transactional=true`

- `fail=true`: симулирует ошибку при сохранении последнего отзыва.
- `transactional=true` (default): при ошибке откат всей транзакции.
- `transactional=false`: при ошибке частичное сохранение.

**Request:** тот же `MovieCreateDto`, поле `reviews` используется.

**Response (201 Created):** `MovieDto`

### Обновить фильм
`PUT /api/movies/{id}`

**Request (MovieDto):**
```json
{
  "id": 1,
  "title": "Interstellar Updated",
  "year": 2014,
  "duration": 170,
  "viewCount": 0,
  "directorId": 1,
  "studioId": 1,
  "genreIds": [1]
}
```

**Response (200 OK):** `MovieDto`

### Частичное обновление фильма
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

**Response (200 OK):** `MovieDto`

### Удалить фильм
`DELETE /api/movies/{id}`

**Response (204 No Content)**

### Загрузить постер
`POST /api/movies/{id}/poster`

**Request (multipart/form-data):**
- `file`: файл изображения

**Response (200 OK):**
```json
{ "url": "/uploads/uuid.jpg" }
```

**Примечание:** доступ к файлу осуществляется по статическому пути `/uploads/**`.

## Directors

### Список режиссеров
`GET /api/directors`

**Response (200 OK):** `List<DirectorDto>`

### Создать режиссера
`POST /api/directors`
```json
{ "fullName": "Christopher Nolan" }
```

**Response (201 Created):** `DirectorDto`

### Обновить режиссера
`PUT /api/directors/{id}`
```json
{ "fullName": "Christopher Nolan Updated" }
```

**Response (200 OK):** `DirectorDto`

### Удалить режиссера
`DELETE /api/directors/{id}`

**Response (204 No Content)**

## Genres

### Список жанров
`GET /api/genres`

**Response (200 OK):** `List<GenreDto>`

### Создать жанр
`POST /api/genres`
```json
{ "name": "Sci-Fi" }
```

**Response (201 Created):** `GenreDto`

### Обновить жанр
`PUT /api/genres/{id}`
```json
{ "name": "Sci-Fi Updated" }
```

**Response (200 OK):** `GenreDto`

### Удалить жанр
`DELETE /api/genres/{id}`

**Response (204 No Content)**

## Studios

### Список студий
`GET /api/studios`

**Response (200 OK):** `List<StudioDto>`

### Создать студию
`POST /api/studios`
```json
{
  "title": "Warner Bros",
  "address": "Hollywood, CA"
}
```

**Response (201 Created):** `StudioDto`

### Обновить студию
`PUT /api/studios/{id}`
```json
{
  "title": "Warner Bros Updated",
  "address": "Burbank, CA"
}
```

**Response (200 OK):** `StudioDto`

### Удалить студию
`DELETE /api/studios/{id}`

**Response (204 No Content)**

## Reviews

### Список отзывов
`GET /api/reviews`

**Response (200 OK):** `List<ReviewDto>`

### Список отзывов к фильму
`GET /api/reviews/movie/{movieId}`

**Response (200 OK):** `List<ReviewDto>`

### Создать отзыв
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

## DTO и валидация (фактические ограничения)

### MovieCreateDto
- `title`: required, not blank
- `year`: required, `>= 1888`
- `duration`: required, `>= 1`
- `viewCount`: required, `>= 0`
- `directorId`, `studioId`: optional
- `genreIds`: optional
- `reviews`: optional (элементы `ReviewDto`)

### MovieDto
- `title`: required
- `year`: required, `>= 1888`, `<= 2027`
- `duration`: required, `>= 1`
- `viewCount`: required, `>= 0`
- `directorId`, `studioId`, `genreIds`: optional

### MoviePatchDto
- все поля опциональны
- `year`: `>= 1888`, `<= 2027`
- `duration`: `>= 1`
- `viewCount`: `>= 0`

### ReviewDto
- `authorAlias`: required
- `rating`: required, `1..10`
- `movieId`: required
- `comment`: optional

### DirectorDto / GenreDto / StudioDto
- `DirectorDto.fullName`: required
- `GenreDto.name`: required
- `StudioDto.title`: required
- `StudioDto.address`: optional
