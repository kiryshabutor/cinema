# Movie Catalog

Справочник фильмов — Spring Boot REST API.

## Лабораторные работы

### Лаба 1 — Basic REST Service
- Spring Boot приложение.
- REST API для сущности **Movie**.
- GET с `@PathVariable` и `@RequestParam`.
- Слои: Controller → Service → Repository.
- DTO + Mapper (MapStruct/Custom).
- Checkstyle.

### Лаба 2 — JPA (Hibernate / Spring Data)
- Подключение PostgreSQL (через Docker Compose).
- Реализация сущностей и связей:
    - **One-to-Many**: Movie ↔ Review
    - **Many-to-One**: Movie → Director, Movie → Studio
    - **Many-to-Many**: Movie ↔ Genre
- CRUD операции для всех сущностей.
- Решение проблемы N+1 (`@EntityGraph`, `JOIN FETCH`).
- Демонстрация транзакционности (`@Transactional`, rollback).

## Сущности

### Movie (Фильм)
| Поле      | Тип     | Описание           |
|-----------|---------|--------------------|
| id        | Long    | PK, auto-generated |
| title     | String  | Название фильма    |
| year      | Integer | Год выпуска        |
| duration  | Integer | Длительность (мин) |
| viewCount | Long    | Счётчик просмотров |
| *relations* | | Director, Studio, Genres, Reviews |

### Другие сущности
- **Director**: Режиссер фильма.
- **Studio**: Студия, снявшая фильм.
- **Genre**: Жанр фильма.
- **Review**: Отзыв пользователя на фильм.

## Запуск

### Требования
- Docker & Docker Compose
- Java 17+ (для локального запуска без Docker)
- Maven

### Быстрый старт (Docker)
Запуск приложения и базы данных в контейнерах:
```bash
docker-compose up --build
```

### Локальный запуск (для разработки)
1. Запустить базу данных:
```bash
docker-compose up -d db
```
2. Запустить приложение:
```bash
mvn spring-boot:run
```

## API и Демонстрации

Полная документация API доступна в файле [api_endpoints.md](api_endpoints.md).

### Демонстрация N+1
- **Плохой запрос**: `/api/demo/n-plus-one` (делает N дополнительных запросов к БД).
- **Хороший запрос**: `/api/demo/join-fetch` (использует `JOIN FETCH` для загрузки данных одним запросом).

### Демонстрация транзакций
- **Создание с ошибкой (без транзакции)**: `/api/movies/with-reviews?transactional=false&fail=true`
    - Фильм сохранится, отзывы сохранятся частично (до ошибки). Данные будут несогласованны.
- **Создание с ошибкой (с транзакцией)**: `/api/movies/with-reviews?transactional=true&fail=true`
    - Произойдет полный откат (Rollback). Ни фильм, ни отзывы не сохранятся.
