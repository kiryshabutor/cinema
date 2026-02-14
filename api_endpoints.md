# API Documentation

Все запросы к API проходят через **Spring Boot Service**.
Base URL: `/` (обычно `http://localhost:8080`)

## Movie Service

### Список фильмов
`GET /api/movies`

Получение списка всех доступных фильмов.

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "title": "Inception",
        "year": 2010,
        "duration": 148,
        "viewCount": 0
    }
]
```

### Получить фильм
`GET /api/movies/:id`

Получение детальной информации о фильме по его ID.

**Response (200 OK):**
```json
{
    "id": 1,
    "title": "Inception",
    "year": 2010,
    "duration": 148,
    "viewCount": 0
}
```

### Поиск фильмов
`GET /api/movies/search`

Поиск фильмов по названию.

**Query Parameters:**
- `title` (required): Часть названия фильма для поиска.

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "title": "Inception",
        "year": 2010,
        "duration": 148,
        "viewCount": 0
    }
]
```

### Создать фильм
`POST /api/movies`

Добавление нового фильма в каталог.

**Request:**
```json
{
    "title": "Interstellar",
    "year": 2014,
    "duration": 169
}
```

**Response (200 OK):**
```json
{
    "id": 2,
    "title": "Interstellar",
    "year": 2014,
    "duration": 169,
    "viewCount": 0
}
```
