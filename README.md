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

[Полная документация API](api_endpoints.md)
