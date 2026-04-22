package com.moviecat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.moviecat.exception.ExternalServiceException;
import com.moviecat.exception.ResourceNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class WikipediaPosterService {

    private static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final RestTemplate restTemplate;

    public DownloadedPoster scrapePoster(String query, Integer year) {
        String normalizedQuery = normalizeQuery(query);
        String resolvedTitle = resolveWikipediaTitle(normalizedQuery, year);
        String imageUrl = resolvePosterImageUrl(resolvedTitle);
        String extension = extractExtension(imageUrl);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported scraped image extension: " + extension);
        }

        byte[] content = downloadImage(imageUrl);
        if (content.length == 0) {
            throw new ExternalServiceException("Wikipedia poster download returned empty body");
        }
        return new DownloadedPoster(extension, content);
    }

    private String resolveWikipediaTitle(String query, Integer year) {
        URI uri = UriComponentsBuilder.fromHttpUrl(WIKIPEDIA_API_URL)
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("utf8", 1)
                .queryParam("srlimit", 10)
                .queryParam("srsearch", buildSearchTerm(query, year))
                .build()
                .encode()
                .toUri();

        JsonNode root = executeGet(uri, "Wikipedia search request failed");
        JsonNode searchItems = root.path("query").path("search");
        if (!searchItems.isArray() || searchItems.isEmpty()) {
            throw new ResourceNotFoundException("Wikipedia page not found for query: " + query);
        }

        String bestTitle = null;
        int bestScore = Integer.MIN_VALUE;
        for (JsonNode item : searchItems) {
            String title = item.path("title").asText("").trim();
            if (title.isEmpty()) {
                continue;
            }
            int score = scoreSearchTitle(title, query, year);
            if (score > bestScore) {
                bestScore = score;
                bestTitle = title;
            }
        }
        if (bestTitle != null) {
            return bestTitle;
        }
        throw new ResourceNotFoundException("Wikipedia search returned no valid page title");
    }

    private String resolvePosterImageUrl(String title) {
        URI uri = UriComponentsBuilder.fromHttpUrl(WIKIPEDIA_API_URL)
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("prop", "pageimages")
                .queryParam("redirects", 1)
                .queryParam("titles", title)
                .queryParam("piprop", "original|thumbnail")
                .queryParam("pithumbsize", 1000)
                .build()
                .encode()
                .toUri();

        JsonNode root = executeGet(uri, "Wikipedia page image request failed");
        JsonNode pages = root.path("query").path("pages");
        if (!pages.isObject()) {
            throw new ResourceNotFoundException("Wikipedia page image metadata not found");
        }

        Iterator<JsonNode> pagesIterator = pages.elements();
        while (pagesIterator.hasNext()) {
            JsonNode page = pagesIterator.next();
            String originalUrl = page.path("original").path("source").asText("").trim();
            if (isAllowedImageUrl(originalUrl)) {
                return originalUrl;
            }
            String thumbnailUrl = page.path("thumbnail").path("source").asText("").trim();
            if (isAllowedImageUrl(thumbnailUrl)) {
                return thumbnailUrl;
            }
        }
        return resolvePosterFromImages(title);
    }

    private String resolvePosterFromImages(String title) {
        URI imagesUri = UriComponentsBuilder.fromHttpUrl(WIKIPEDIA_API_URL)
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("prop", "images")
                .queryParam("redirects", 1)
                .queryParam("imlimit", "max")
                .queryParam("titles", title)
                .build()
                .encode()
                .toUri();

        JsonNode imagesRoot = executeGet(imagesUri, "Wikipedia page images request failed");
        JsonNode pages = imagesRoot.path("query").path("pages");
        if (!pages.isObject()) {
            throw new ResourceNotFoundException("Poster image not found on Wikipedia page: " + title);
        }

        List<String> candidates = new ArrayList<>();
        Iterator<JsonNode> pagesIterator = pages.elements();
        while (pagesIterator.hasNext()) {
            JsonNode page = pagesIterator.next();
            JsonNode images = page.path("images");
            if (!images.isArray()) {
                continue;
            }
            for (JsonNode image : images) {
                String imageTitle = image.path("title").asText("").trim();
                if (isAllowedImageTitle(imageTitle)) {
                    candidates.add(imageTitle);
                }
            }
        }

        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("Poster image not found on Wikipedia page: " + title);
        }

        String prioritized = candidates.stream()
                .filter(candidate -> candidate.toLowerCase(Locale.ROOT).contains("poster"))
                .findFirst()
                .orElse(candidates.get(0));
        return resolveImageInfoUrl(prioritized, title);
    }

    private String resolveImageInfoUrl(String imageTitle, String pageTitle) {
        URI imageInfoUri = UriComponentsBuilder.fromHttpUrl(WIKIPEDIA_API_URL)
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("titles", imageTitle)
                .build()
                .encode()
                .toUri();

        JsonNode imageInfoRoot = executeGet(imageInfoUri, "Wikipedia imageinfo request failed");
        JsonNode pages = imageInfoRoot.path("query").path("pages");
        if (!pages.isObject()) {
            throw new ResourceNotFoundException("Poster image not found on Wikipedia page: " + pageTitle);
        }

        Iterator<JsonNode> pagesIterator = pages.elements();
        while (pagesIterator.hasNext()) {
            JsonNode page = pagesIterator.next();
            JsonNode imageInfo = page.path("imageinfo");
            if (!imageInfo.isArray() || imageInfo.isEmpty()) {
                continue;
            }
            String imageUrl = imageInfo.get(0).path("url").asText("").trim();
            if (!imageUrl.isEmpty()) {
                return imageUrl;
            }
        }
        throw new ResourceNotFoundException("Poster image not found on Wikipedia page: " + pageTitle);
    }

    private boolean isAllowedImageTitle(String imageTitle) {
        if (imageTitle == null || imageTitle.isBlank()) {
            return false;
        }
        String normalized = imageTitle.toLowerCase(Locale.ROOT);
        for (String extension : ALLOWED_EXTENSIONS) {
            if (normalized.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        try {
            return ALLOWED_EXTENSIONS.contains(extractExtension(imageUrl));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] downloadImage(String imageUrl) {
        URI uri = URI.create(imageUrl);
        try {
            byte[] content = restTemplate.getForObject(uri, byte[].class);
            if (content == null) {
                throw new ExternalServiceException("Wikipedia poster download returned null body");
            }
            return content;
        } catch (RestClientResponseException exception) {
            String message = "Wikipedia poster download failed (status=%d)"
                    .formatted(exception.getStatusCode().value());
            throw new ExternalServiceException(message, exception);
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Wikipedia poster download failed", exception);
        }
    }

    private JsonNode executeGet(URI uri, String errorMessage) {
        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            if (response == null) {
                throw new ExternalServiceException(errorMessage + ": empty response body");
            }
            return response;
        } catch (RestClientResponseException exception) {
            String message = "%s (status=%d)".formatted(errorMessage, exception.getStatusCode().value());
            throw new ExternalServiceException(message, exception);
        } catch (RestClientException exception) {
            throw new ExternalServiceException(errorMessage, exception);
        }
    }

    private String buildSearchTerm(String query, Integer year) {
        String yearSuffix = year == null ? "" : " " + year;
        return query + yearSuffix + " film";
    }

    private int scoreSearchTitle(String title, String query, Integer year) {
        String normalizedTitle = title.toLowerCase(Locale.ROOT);
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        int score = 0;

        if (normalizedTitle.equals(normalizedQuery)) {
            score += 200;
        }
        if (normalizedTitle.contains(normalizedQuery)) {
            score += 80;
        }
        if (normalizedTitle.startsWith(normalizedQuery)) {
            score += 30;
        }
        if (normalizedTitle.contains("(film)")) {
            score += 60;
        }
        if (year != null && normalizedTitle.contains("(" + year + " film)")) {
            score += 160;
        }
        if (normalizedTitle.startsWith("list of ")) {
            score -= 220;
        }
        if (normalizedTitle.contains("video game")) {
            score -= 180;
        }
        if (normalizedTitle.contains("soundtrack")) {
            score -= 140;
        }
        if (normalizedTitle.contains("disambiguation")) {
            score -= 250;
        }
        return score;
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        return query.trim();
    }

    private String extractExtension(String url) {
        String normalizedUrl = Objects.requireNonNull(url, "url").toLowerCase(Locale.ROOT);
        int fragmentIndex = normalizedUrl.indexOf('#');
        String noFragment = fragmentIndex >= 0 ? normalizedUrl.substring(0, fragmentIndex) : normalizedUrl;
        int queryIndex = noFragment.indexOf('?');
        String path = queryIndex >= 0 ? noFragment.substring(0, queryIndex) : noFragment;
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0 || lastDot == path.length() - 1) {
            throw new IllegalArgumentException("Unable to detect image extension from URL");
        }
        return path.substring(lastDot);
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
}
