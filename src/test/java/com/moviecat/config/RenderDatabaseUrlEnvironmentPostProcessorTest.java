package com.moviecat.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class RenderDatabaseUrlEnvironmentPostProcessorTest {

    private final RenderDatabaseUrlEnvironmentPostProcessor postProcessor =
            new RenderDatabaseUrlEnvironmentPostProcessor();

    @Test
    void shouldConvertLegacyDatasourceUrlWhenItUsesPostgresqlScheme() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "postgresql://user:pass@host:5432/db");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("jdbc:postgresql://user:pass@host:5432/db", environment.getProperty("spring.datasource.url"));
    }

    @Test
    void shouldBuildDatasourceUrlFromRenderDatabaseUrlWhenNeeded() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("RENDER_DATABASE_URL", "postgresql://user:pass@host:5432/db");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("jdbc:postgresql://user:pass@host:5432/db", environment.getProperty("spring.datasource.url"));
    }

    @Test
    void shouldKeepJdbcDatasourceUrlUntouched() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://user:pass@host:5432/db");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("jdbc:postgresql://user:pass@host:5432/db", environment.getProperty("spring.datasource.url"));
    }
}
