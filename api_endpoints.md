# Movie Catalog API

## Endpoints

### Movies

| Method | URL                     | Description                   |
|--------|-------------------------|-------------------------------|
| GET    | `/api/movies`           | Get all movies                |
| GET    | `/api/movies/{id}`      | Get movie by ID (`@PathVariable`) |
| GET    | `/api/movies/search?title=...` | Search movies by title (`@RequestParam`) |
| POST   | `/api/movies`           | Create a new movie (`@RequestBody`) |

### Examples

```bash
# Get all movies
curl http://localhost:8080/api/movies

# Get movie by ID
curl http://localhost:8080/api/movies/1

# Search by title
curl "http://localhost:8080/api/movies/search?title=avatar"

# Create a movie
curl -X POST -H "Content-Type: application/json" -d '{"title": "Inception", "year": 2010, "duration": 148, "viewCount": 0}' http://localhost:8080/api/movies

# Response example:
# {
#   "id": 1,
#   "title": "Inception",
#   "year": 2010,
#   "duration": 148,
#   "viewCount": 0
# }
```
