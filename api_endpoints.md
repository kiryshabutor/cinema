# API Documentation

Все запросы к API проходят через **Spring Boot Service**.
Base URL: `/` (обычно `http://localhost:8080`)

## Movies

### Список фильмов
`GET /api/movies`

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

**Response:**
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

**Response:**
Возвращает созданный объект `Movie` (без вложенного списка отзывов).
```json
{
    "id": 1,
    "title": "Inception",
    "year": 2010,
    "duration": 148,
    "viewCount": 0,
    "posterUrl": null,
    "directorId": 1,
    "directorName": "Christopher Nolan",
    "studioId": 1,
    "studioTitle": "Warner Bros",
    "genreIds": [1],
    "genreNames": ["Sci-Fi"]
}
```



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

## Demo (N+1 Problem)

### Проблема N+1 (Плохо)
`GET /api/demo/n-plus-one`
Демонстрация проблемы N+1. Вызывает множество SELECT запросов.

### Решение N+1 (Хорошо)
`GET /api/demo/join-fetch`
Решение проблемы N+1. Вызывает один SELECT запрос с JOIN.
 
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
