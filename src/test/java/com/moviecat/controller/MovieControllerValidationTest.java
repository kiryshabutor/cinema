package com.moviecat.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.moviecat.exception.GlobalExceptionHandler;
import com.moviecat.service.MovieService;
import com.moviecat.service.task.ReviewTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MovieControllerValidationTest {

    private MovieService movieService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        movieService = Mockito.mock(MovieService.class);
        ReviewTaskService reviewTaskService = Mockito.mock(ReviewTaskService.class);
        MovieController movieController = new MovieController(movieService, reviewTaskService);
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void runViewRaceDemo_shouldReturnBadRequest_whenThreadsLessThanFifty() throws Exception {
        mockMvc.perform(post("/api/movies/1/views/race-demo")
                        .param("mode", "safe")
                        .param("threads", "49")
                        .param("incrementsPerThread", "1000"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieService);
    }

    @Test
    void runViewRaceDemo_shouldReturnBadRequest_whenIncrementsPerThreadIsZero() throws Exception {
        mockMvc.perform(post("/api/movies/1/views/race-demo")
                        .param("mode", "safe")
                        .param("threads", "50")
                        .param("incrementsPerThread", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieService);
    }
}
