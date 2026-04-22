package com.moviecat.config;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "renderDatabaseUrlOverride";
    private static final String DATASOURCE_URL_KEY = "spring.datasource.url";
    private static final String RENDER_DATABASE_URL_KEY = "RENDER_DATABASE_URL";
    private static final String POSTGRES_SCHEME = "postgresql://";
    private static final String JDBC_POSTGRES_SCHEME = "jdbc:postgresql://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configuredUrl = environment.getProperty(DATASOURCE_URL_KEY);
        String renderDatabaseUrl = environment.getProperty(RENDER_DATABASE_URL_KEY);

        if (configuredUrl != null && configuredUrl.startsWith(POSTGRES_SCHEME)) {
            environment.getPropertySources().addFirst(new MapPropertySource(
                    PROPERTY_SOURCE_NAME,
                    Map.of(DATASOURCE_URL_KEY, JDBC_POSTGRES_SCHEME + configuredUrl.substring(POSTGRES_SCHEME.length()))));
            return;
        }

        if (renderDatabaseUrl != null && renderDatabaseUrl.startsWith(POSTGRES_SCHEME)) {
            environment.getPropertySources().addFirst(new MapPropertySource(
                    PROPERTY_SOURCE_NAME,
                    Map.of(
                            DATASOURCE_URL_KEY,
                            JDBC_POSTGRES_SCHEME + renderDatabaseUrl.substring(POSTGRES_SCHEME.length()))));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
