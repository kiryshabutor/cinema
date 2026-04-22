package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.moviecat.exception.ResourceNotFoundException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class WikipediaPosterServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private WikipediaPosterService wikipediaPosterService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        wikipediaPosterService = new WikipediaPosterService(restTemplate);
    }

    @Test
    void scrapePoster_shouldResolveTitleAndDownloadPoster() {
        String searchResponse = """
                {
                  "query": {
                    "search": [
                      { "title": "Interstellar" }
                    ]
                  }
                }
                """;
        String imageResponse = """
                {
                  "query": {
                    "pages": {
                      "157336": {
                        "pageid": 157336,
                        "title": "Interstellar",
                        "original": {
                          "source": "https://upload.wikimedia.org/posters/interstellar.jpg"
                        }
                      }
                    }
                  }
                }
                """;

        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&srlimit=10"
                        + "&srsearch=Interstellar%202014%20film"))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&redirects=1"
                        + "&titles=Interstellar&piprop=original%7Cthumbnail&pithumbsize=1000"))
                .andRespond(withSuccess(imageResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://upload.wikimedia.org/posters/interstellar.jpg"))
                .andRespond(withSuccess("img-bytes", MediaType.IMAGE_JPEG));

        WikipediaPosterService.DownloadedPoster result = wikipediaPosterService.scrapePoster("Interstellar", 2014);

        assertEquals(".jpg", result.extension());
        assertEquals("img-bytes", new String(result.content(), StandardCharsets.UTF_8));
        server.verify();
    }

    @Test
    void scrapePoster_shouldPreferFilmPageOverUnrelatedSearchResult() {
        String searchResponse = """
                {
                  "query": {
                    "search": [
                      { "title": "List of Terminator video games" },
                      { "title": "Terminator 2: Judgment Day" }
                    ]
                  }
                }
                """;
        String imageResponse = """
                {
                  "query": {
                    "pages": {
                      "157336": {
                        "pageid": 157336,
                        "title": "Terminator 2: Judgment Day",
                        "original": {
                          "source": "https://upload.wikimedia.org/posters/t2.jpg"
                        }
                      }
                    }
                  }
                }
                """;

        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&srlimit=10"
                        + "&srsearch=Terminator%202:%20Judgment%20Day%201991%20film"))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&redirects=1"
                        + "&titles=Terminator%202:%20Judgment%20Day&piprop=original%7Cthumbnail"
                        + "&pithumbsize=1000"))
                .andRespond(withSuccess(imageResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://upload.wikimedia.org/posters/t2.jpg"))
                .andRespond(withSuccess("t2-bytes", MediaType.IMAGE_JPEG));

        WikipediaPosterService.DownloadedPoster result =
                wikipediaPosterService.scrapePoster("Terminator 2: Judgment Day", 1991);

        assertEquals(".jpg", result.extension());
        assertEquals("t2-bytes", new String(result.content(), StandardCharsets.UTF_8));
        server.verify();
    }

    @Test
    void scrapePoster_shouldFallbackToPageImagesListWhenPrimaryImageIsSvg() {
        String searchResponse = """
                {
                  "query": {
                    "search": [
                      { "title": "Finding Nemo" }
                    ]
                  }
                }
                """;
        String imageResponse = """
                {
                  "query": {
                    "pages": {
                      "12": {
                        "pageid": 12,
                        "title": "Finding Nemo",
                        "original": {
                          "source": "https://upload.wikimedia.org/wikipedia/en/f/fb/Finding_Nemo_logo.svg"
                        }
                      }
                    }
                  }
                }
                """;
        String imagesListResponse = """
                {
                  "query": {
                    "pages": {
                      "12": {
                        "pageid": 12,
                        "title": "Finding Nemo",
                        "images": [
                          { "title": "File:Finding Nemo poster.jpg" }
                        ]
                      }
                    }
                  }
                }
                """;
        String imageInfoResponse = """
                {
                  "query": {
                    "pages": {
                      "-1": {
                        "title": "File:Finding Nemo poster.jpg",
                        "imageinfo": [
                          { "url": "https://upload.wikimedia.org/posters/finding-nemo.jpg" }
                        ]
                      }
                    }
                  }
                }
                """;

        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&srlimit=10"
                        + "&srsearch=Finding%20Nemo%202003%20film"))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&redirects=1"
                        + "&titles=Finding%20Nemo&piprop=original%7Cthumbnail&pithumbsize=1000"))
                .andRespond(withSuccess(imageResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=images&redirects=1"
                        + "&imlimit=max&titles=Finding%20Nemo"))
                .andRespond(withSuccess(imagesListResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=imageinfo&iiprop=url"
                        + "&titles=File:Finding%20Nemo%20poster.jpg"))
                .andRespond(withSuccess(imageInfoResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://upload.wikimedia.org/posters/finding-nemo.jpg"))
                .andRespond(withSuccess("nemo-bytes", MediaType.IMAGE_JPEG));

        WikipediaPosterService.DownloadedPoster result = wikipediaPosterService.scrapePoster("Finding Nemo", 2003);

        assertEquals(".jpg", result.extension());
        assertEquals("nemo-bytes", new String(result.content(), StandardCharsets.UTF_8));
        server.verify();
    }

    @Test
    void scrapePoster_shouldThrowWhenSearchHasNoResults() {
        String searchResponse = """
                {
                  "query": {
                    "search": []
                  }
                }
                """;

        server.expect(requestTo(
                "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&srlimit=10"
                        + "&srsearch=Unknown%20film"))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> wikipediaPosterService.scrapePoster("Unknown", null));
    }

    @Test
    void scrapePoster_shouldThrowForBlankQuery() {
        assertThrows(IllegalArgumentException.class, () -> wikipediaPosterService.scrapePoster("  ", 2014));
    }

    @Test
    void downloadedPoster_shouldCompareByteArrayByContent() {
        WikipediaPosterService.DownloadedPoster left =
                new WikipediaPosterService.DownloadedPoster(".png", "img".getBytes(StandardCharsets.UTF_8));
        WikipediaPosterService.DownloadedPoster right =
                new WikipediaPosterService.DownloadedPoster(".png", "img".getBytes(StandardCharsets.UTF_8));
        WikipediaPosterService.DownloadedPoster different =
                new WikipediaPosterService.DownloadedPoster(".png", "other".getBytes(StandardCharsets.UTF_8));

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, different);
        assertEquals("DownloadedPoster[extension=.png, content=[105, 109, 103]]", left.toString());
    }
}
