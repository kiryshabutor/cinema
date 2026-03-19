package com.moviecat.service.cache;

import com.moviecat.dto.MovieResponseDto;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieSearchCache {

    private final Map<MovieSearchKey, Page<MovieResponseDto>> cache = Collections.synchronizedMap(new HashMap<>());

    public record LookupResult(Page<MovieResponseDto> page, boolean cacheHit) {
    }

    public Page<MovieResponseDto> get(MovieSearchKey key) {
        return cache.get(key);
    }

    public void put(MovieSearchKey key, Page<MovieResponseDto> value) {
        cache.put(key, value);
    }

    public LookupResult getOrCompute(MovieSearchKey key, Supplier<Page<MovieResponseDto>> loader) {
        AtomicBoolean computed = new AtomicBoolean(false);
        Page<MovieResponseDto> page = cache.computeIfAbsent(key, ignored -> {
            computed.set(true);
            Page<MovieResponseDto> loadedPage = loader.get();
            return Objects.requireNonNull(loadedPage, "loadedPage");
        });
        return new LookupResult(page, !computed.get());
    }

    public void clear() {
        invalidate("unspecified");
    }

    public void invalidate(String reason) {
        int sizeBefore = cache.size();
        cache.clear();
        log.info("CACHE INVALIDATED: reason='{}', clearedEntries={}", reason, sizeBefore);
    }
}
