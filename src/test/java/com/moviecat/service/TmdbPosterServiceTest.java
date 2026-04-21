package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.moviecat.config.TmdbProperties;
import com.moviecat.dto.TmdbPosterCandidateDto;
import com.moviecat.exception.ExternalServiceException;
import com.moviecat.exception.ServiceUnavailableException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class TmdbPosterServiceTest {

    private TmdbProperties tmdbProperties;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private TmdbPosterService tmdbPosterService;

    @BeforeEach
    void setUp() {
        tmdbProperties = new TmdbProperties();
        tmdbProperties.setApiKey("test-key");
        tmdbProperties.setApiBaseUrl("https://tmdb.example.test/3");
        tmdbProperties.setImageBaseUrl("https://img.example.test/w500");
        tmdbProperties.setLanguage("en-US");

        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        tmdbPosterService = new TmdbPosterService(restTemplate, tmdbProperties);
    }

    @Test
    void searchPosters_shouldReturnMappedCandidates() {
        String response = """
                {
                  "results": [
                    {
                      "id": 157336,
                      "title": "Interstellar",
                      "release_date": "2014-11-05",
                      "poster_path": "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"
                    },
                    {
                      "id": 2,
                      "title": "No Poster",
                      "release_date": "2010-01-01",
                      "poster_path": ""
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://tmdb.example.test/3/search/movie?api_key=test-key&query=Interstellar"
                + "&language=en-US&include_adult=false&year=2014"))
                .andExpect(queryParam("query", "Interstellar"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        List<TmdbPosterCandidateDto> result = tmdbPosterService.searchPosters("Interstellar", 2014);

        assertEquals(1, result.size());
        assertEquals(157336L, result.get(0).getTmdbId());
        assertEquals("Interstellar", result.get(0).getTitle());
        assertEquals(2014, result.get(0).getReleaseYear());
        assertEquals("/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", result.get(0).getPosterPath());
        assertEquals(
                "https://img.example.test/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                result.get(0).getPreviewUrl());
        server.verify();
    }

    @Test
    void searchPosters_shouldThrowWhenApiKeyMissing() {
        tmdbProperties.setApiKey("   ");

        assertThrows(ServiceUnavailableException.class, () -> tmdbPosterService.searchPosters("Dune", null));
    }

    @Test
    void searchPosters_shouldThrowWhenUpstreamFails() {
        server.expect(requestTo("https://tmdb.example.test/3/search/movie?api_key=test-key&query=Dune"
                + "&language=en-US&include_adult=false"))
                .andRespond(withServerError());

        assertThrows(ExternalServiceException.class, () -> tmdbPosterService.searchPosters("Dune", null));
    }

    @Test
    void downloadPoster_shouldReturnContentAndExtension() {
        byte[] body = "image".getBytes(StandardCharsets.UTF_8);
        server.expect(requestTo("https://img.example.test/w500/poster.jpg"))
                .andRespond(withSuccess(body, MediaType.IMAGE_JPEG));

        TmdbPosterService.DownloadedPoster poster = tmdbPosterService.downloadPoster("/poster.jpg");

        assertEquals(".jpg", poster.extension());
        assertEquals("image", new String(poster.content(), StandardCharsets.UTF_8));
        server.verify();
    }

    @Test
    void downloadPoster_shouldThrowWhenPathIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> tmdbPosterService.downloadPoster("poster.jpg"));
    }
}
