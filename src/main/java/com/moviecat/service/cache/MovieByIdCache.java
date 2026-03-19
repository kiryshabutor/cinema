package com.moviecat.service.cache;

import com.moviecat.dto.MovieResponseDto;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieByIdCache {

    private final Map<Long, MovieResponseDto> cache = Collections.synchronizedMap(new HashMap<>());

    public record LookupResult(MovieResponseDto movie, boolean cacheHit) {
    }

    public LookupResult getOrCompute(Long id, Supplier<MovieResponseDto> loader) {
        AtomicBoolean computed = new AtomicBoolean(false);
        MovieResponseDto movie = cache.computeIfAbsent(id, ignored -> {
            computed.set(true);
            MovieResponseDto loadedMovie = loader.get();
            return Objects.requireNonNull(loadedMovie, "loadedMovie");
        });
        return new LookupResult(movie, !computed.get());
    }

    public void clear() {
        invalidate("unspecified");
    }

    public void invalidate(String reason) {
        int sizeBefore = cache.size();
        cache.clear();
        log.info("MOVIE BY ID CACHE INVALIDATED: reason='{}', clearedEntries={}", reason, sizeBefore);
    }
}
