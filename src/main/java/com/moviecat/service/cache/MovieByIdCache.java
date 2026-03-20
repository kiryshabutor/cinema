package com.moviecat.service.cache;

import com.moviecat.dto.MovieResponseDto;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieByIdCache {

    private final Map<Long, MovieResponseDto> cache = Collections.synchronizedMap(new HashMap<>());

    public MovieResponseDto get(Long id) {
        return cache.get(id);
    }

    public void put(Long id, MovieResponseDto value) {
        cache.put(id, value);
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
