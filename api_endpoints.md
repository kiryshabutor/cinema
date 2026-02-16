# API Documentation

Все запросы к API проходят через **Spring Boot Service**.
Base URL: `/` (обычно `http://localhost:8080`)

## Movies

### Список фильмов (с демонстрацией N+1)
`GET /api/movies?fetchType={type}`

*   `fetchType=eager` (default): Использует оптимизированный запрос (JOIN FETCH). Проблема N+1 решена.
*   `fetchType=lazy`: Использует стандартный `findAll()`. Демонстрирует проблему N+1 (множество запросов к БД).

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "title": "Inception",
        "year": 2010,
        "duration": 148,
        "viewCount": 0,
        "posterUrl": "/uploads/example.jpg",
        "directorId": 1,
        "directorName": "Christopher Nolan",
        "studioId": 1,
        "studioTitle": "Warner Bros",
        "genreIds": [1],
        "genreNames": ["Sci-Fi"]
    }
]
```

### Поиск фильмов по названию
`GET /api/movies/search?title={title}`

**Response (201 Created):**
Список фильмов, название которых содержит переданную подстроку.

### Получить фильм по ID
`GET /api/movies/{id}`

**Response (201 Created):** One `MovieDto` object.

### Создать фильм
`POST /api/movies`

**Request:**
```json
{
    "title": "Interstellar",
    "year": 2014,
    "duration": 169,
    "directorId": 1,
    "studioId": 1,
    "genreIds": [1],
    "viewCount": 0
}
```

### Обновить фильм
`PUT /api/movies/{id}`

**Request:**
```json
{
    "title": "Interstellar Updated",
    "year": 2014,
    "duration": 170,
    "directorId": 1,
    "studioId": 1,
    "genreIds": [1],
    "viewCount": 0
}
```

### Частичное обновление фильма
`PATCH /api/movies/{id}`
Частично обновляет данные о фильме. Обновляются только переданные поля.

**Тело запроса (JSON):**
(Все поля опциональны)
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

**Ответ:**
То же самое, что и при создании (Updated MovieDto).

### Удалить фильм
`DELETE /api/movies/{id}`
Удаляет фильм по ID.
**Успешный ответ:** `200 OK`

### Загрузить постер
`POST /api/movies/{id}/poster`

Загружает изображение (файл) для фильма.
**Request (multipart/form-data):**
*   `file`: Файл изображения

**Response (201 Created):**
*   Строка с URL загруженного файла (например, `/uploads/uuid.jpg`).

### Создать фильм с отзывами
`POST /api/movies/with-reviews?fail=false&transactional=true`

Создает фильм и сразу список отзывов к нему.
*   `fail=true`: Симулирует ошибку при сохранении последнего отзыва.
*   `transactional=true` (default): В случае ошибки происходит полный откат (фильм не создается).
*   `transactional=false`: В случае ошибки происходит частичное сохранение (фильм и часть отзывов остаются в БД).

**Request:**
```json
{
    "title": "Inception",
    "year": 2010,
    "duration": 148,
    "directorId": 1,
    "studioId": 1,
    "genreIds": [1],
    "reviews": [
        {"authorAlias": "User1", "rating": 10, "comment": "Great!"},
        {"authorAlias": "User2", "rating": 9, "comment": "Good"}
    ]
}
```

**Response (201 Created):**
Возвращает созданный объект `Movie` (без вложенного списка отзывов).

## Directors

### Список режиссеров
`GET /api/directors`

### Создать режиссера
`POST /api/directors`
```json
{
  "fullName": "Christopher Nolan"
}
```

### Обновить режиссера
`PUT /api/directors/{id}`
```json
{
  "fullName": "Christopher Nolan Updated"
}
```

### Удалить режиссера
`DELETE /api/directors/{id}`
**Response (201 Created):** `204 No Content`

## Genres

### Список жанров
`GET /api/genres`

### Создать жанр
`POST /api/genres`
```json
{
  "name": "Sci-Fi"
}
```

### Обновить жанр
`PUT /api/genres/{id}`
```json
{
  "name": "Sci-Fi Updated"
}
```

### Удалить жанр
`DELETE /api/genres/{id}`
**Response (201 Created):** `204 No Content`

## Studios

### Список студий
`GET /api/studios`

### Создать студию
`POST /api/studios`
```json
{
  "title": "Warner Bros",
  "address": "Hollywood, CA"
}
```

### Обновить студию
`PUT /api/studios/{id}`
```json
{
  "title": "Warner Bros Updated",
  "address": "Burbank, CA"
}
```

### Удалить студию
`DELETE /api/studios/{id}`
**Response (201 Created):** `204 No Content`

## Reviews

### Список отзывов
`GET /api/reviews`

### Список отзывов к фильму
`GET /api/reviews/movie/{movieId}`

### Создать отзыв
`POST /api/reviews`

**Request:**
```json
{
  "movieId": 1,
  "authorAlias": "Critic1",
  "rating": 8,
  "comment": "Nice movie!"
}
```



