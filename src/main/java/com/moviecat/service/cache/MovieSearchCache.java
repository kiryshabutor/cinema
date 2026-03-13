package com.moviecat.service.cache;

import com.moviecat.dto.MovieResponseDto;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieSearchCache {

    private final Map<MovieSearchKey, Page<MovieResponseDto>> cache =
            Collections.synchronizedMap(new HashMap<>());

    public Page<MovieResponseDto> get(MovieSearchKey key) {
        return cache.get(key);
    }

    public void put(MovieSearchKey key, Page<MovieResponseDto> value) {
        cache.put(key, value);
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
