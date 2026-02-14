# Movie Catalog

Справочник фильмов — Spring Boot REST API.

## Лаба 1 — Basic REST Service

- Spring Boot приложение
- REST API для сущности **Movie**
- GET с `@PathVariable` и `@RequestParam`
- Слои: Controller → Service → Repository
- DTO + Mapper
- Checkstyle

## Сущность Movie

| Поле      | Тип     | Описание           |
|-----------|---------|--------------------|
| id        | Long    | PK, auto-generated |
| title     | String  | Название фильма    |
| year      | Integer | Год выпуска        |
| duration  | Integer | Длительность (мин) |
| viewCount | Long    | Счётчик просмотров |

## Запуск

```bash
# БД
docker-compose up -d

# Приложение
mvn spring-boot:run
```

## API

- `GET /api/movies` — все фильмы
- `GET /api/movies/{id}` — фильм по ID
- `GET /api/movies/search?title=...` — поиск по названию
- `POST /api/movies` — добавить фильм
