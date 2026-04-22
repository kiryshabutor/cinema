package com.moviecat.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviecat.config.TmdbProperties;
import com.moviecat.dto.TmdbPosterCandidateDto;
import com.moviecat.exception.ExternalServiceException;
import com.moviecat.exception.ServiceUnavailableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class TmdbPosterService {

    private static final String SEARCH_PATH = "/search/movie";
    private static final int MAX_RESULTS = 20;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final RestTemplate restTemplate;
    private final TmdbProperties tmdbProperties;

    public List<TmdbPosterCandidateDto> searchPosters(String query, Integer year) {
        ensureApiKeyConfigured();
        String normalizedQuery = normalizeQuery(query);
        URI uri = buildSearchUri(normalizedQuery, year);

        TmdbSearchResponse response = executeGet(uri, TmdbSearchResponse.class, "TMDB search failed");
        List<TmdbSearchResultItem> results = response != null ? response.getResults() : List.of();

        List<TmdbPosterCandidateDto> candidates = new ArrayList<>();
        for (TmdbSearchResultItem result : results) {
            TmdbPosterCandidateDto candidate = toCandidate(result);
            if (candidate != null) {
                candidates.add(candidate);
            }
            if (candidates.size() >= MAX_RESULTS) {
                break;
            }
        }
        return candidates;
    }

    public DownloadedPoster downloadPoster(String posterPath) {
        ensureApiKeyConfigured();
        String normalizedPosterPath = normalizePosterPath(posterPath);
        String extension = extractExtension(normalizedPosterPath);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported poster extension: " + extension);
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(tmdbProperties.getImageBaseUrl())
                .path(normalizedPosterPath)
                .build(true)
                .toUri();
        byte[] content = executeGet(uri, byte[].class, "TMDB poster download failed");
        if (content == null || content.length == 0) {
            throw new ExternalServiceException("TMDB poster download returned empty body");
        }
        return new DownloadedPoster(extension, content);
    }

    private URI buildSearchUri(String query, Integer year) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tmdbProperties.getApiBaseUrl())
                .path(SEARCH_PATH)
                .queryParam("api_key", tmdbProperties.getApiKey())
                .queryParam("query", query)
                .queryParam("language", tmdbProperties.getLanguage())
                .queryParam("include_adult", false);
        if (year != null) {
            builder.queryParam("year", year);
        }
        return builder.build(true).toUri();
    }

    private <T> T executeGet(URI uri, Class<T> responseType, String errorMessage) {
        try {
            return restTemplate.getForObject(uri, responseType);
        } catch (RestClientResponseException exception) {
            String message = "%s (status=%d)".formatted(errorMessage, exception.getStatusCode().value());
            throw new ExternalServiceException(message, exception);
        } catch (RestClientException exception) {
            throw new ExternalServiceException(errorMessage, exception);
        }
    }

    private TmdbPosterCandidateDto toCandidate(TmdbSearchResultItem result) {
        if (result == null || result.getId() == null) {
            return null;
        }
        String posterPath = normalizeOptional(result.getPosterPath());
        if (posterPath.isEmpty()) {
            return null;
        }

        String title = normalizeOptional(result.getTitle());
        if (title.isEmpty()) {
            title = normalizeOptional(result.getOriginalTitle());
        }
        if (title.isEmpty()) {
            title = "Untitled";
        }

        Integer releaseYear = parseYear(result.getReleaseDate());
        String previewUrl = buildPreviewUrl(posterPath);
        return new TmdbPosterCandidateDto(result.getId(), title, releaseYear, posterPath, previewUrl);
    }

    private String buildPreviewUrl(String posterPath) {
        URI uri = UriComponentsBuilder.fromHttpUrl(tmdbProperties.getImageBaseUrl())
                .path(posterPath)
                .build(true)
                .toUri();
        return uri.toString();
    }

    private String normalizeQuery(String query) {
        String normalized = normalizeOptional(query);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        return normalized;
    }

    private String normalizePosterPath(String posterPath) {
        String normalized = normalizeOptional(posterPath);
        if (normalized.isEmpty() || !normalized.startsWith("/")) {
            throw new IllegalArgumentException("posterPath must start with '/'");
        }
        if (normalized.contains("..") || normalized.contains("?") || normalized.contains("#")) {
            throw new IllegalArgumentException("posterPath contains forbidden characters");
        }
        return normalized;
    }

    private String extractExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0 || lastDot == path.length() - 1) {
            throw new IllegalArgumentException("posterPath must contain a file extension");
        }
        return path.substring(lastDot).toLowerCase();
    }

    private Integer parseYear(String releaseDate) {
        String normalizedDate = normalizeOptional(releaseDate);
        if (normalizedDate.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(normalizedDate.substring(0, 4));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private void ensureApiKeyConfigured() {
        if (normalizeOptional(tmdbProperties.getApiKey()).isEmpty()) {
            throw new ServiceUnavailableException("TMDB API key is not configured");
        }
    }

    public record DownloadedPoster(@NonNull String extension, @NonNull byte[] content) {

        public DownloadedPoster {
            Objects.requireNonNull(extension, "extension");
            Objects.requireNonNull(content, "content");
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof DownloadedPoster that)) {
                return false;
            }
            return extension.equals(that.extension) && Arrays.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return 31 * extension.hashCode() + Arrays.hashCode(content);
        }

        @Override
        public String toString() {
            return "DownloadedPoster[extension=" + extension + ", content=" + Arrays.toString(content) + "]";
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TmdbSearchResponse {

        private List<TmdbSearchResultItem> results = List.of();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TmdbSearchResultItem {

        private Long id;
        private String title;
        @JsonProperty("original_title")
        private String originalTitle;
        @JsonProperty("release_date")
        private String releaseDate;
        @JsonProperty("poster_path")
        private String posterPath;
    }
}
