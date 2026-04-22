package com.moviecat.config;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "renderDatabaseUrlOverride";
    private static final String DATASOURCE_URL_KEY = "spring.datasource.url";
    private static final String DATASOURCE_USERNAME_KEY = "spring.datasource.username";
    private static final String DATASOURCE_PASSWORD_KEY = "spring.datasource.password";
    private static final String RENDER_DATABASE_URL_KEY = "RENDER_DATABASE_URL";
    private static final String POSTGRES_SCHEME = "postgresql://";
    private static final String JDBC_POSTGRES_SCHEME = "jdbc:postgresql://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configuredUrl = environment.getProperty(DATASOURCE_URL_KEY);
        String renderDatabaseUrl = environment.getProperty(RENDER_DATABASE_URL_KEY);

        if (configuredUrl != null && configuredUrl.startsWith(POSTGRES_SCHEME)) {
            applyDatabaseProperties(environment, configuredUrl, false);
            return;
        }

        if (renderDatabaseUrl != null && renderDatabaseUrl.startsWith(POSTGRES_SCHEME)) {
            applyDatabaseProperties(environment, renderDatabaseUrl, true);
        }
    }

    private void applyDatabaseProperties(
            ConfigurableEnvironment environment, String databaseUrl, boolean fillMissingCredentialsOnly) {
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put(DATASOURCE_URL_KEY, JDBC_POSTGRES_SCHEME + databaseUrl.substring(POSTGRES_SCHEME.length()));

        URI uri = URI.create(databaseUrl);
        String userInfo = uri.getUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
            return;
        }

        int separatorIndex = userInfo.indexOf(':');
        String username = separatorIndex >= 0 ? userInfo.substring(0, separatorIndex) : userInfo;
        String password = separatorIndex >= 0 ? userInfo.substring(separatorIndex + 1) : "";

        if (!fillMissingCredentialsOnly || isBlank(environment.getProperty(DATASOURCE_USERNAME_KEY))) {
            overrides.put(DATASOURCE_USERNAME_KEY, username);
        }
        if (!fillMissingCredentialsOnly || isBlank(environment.getProperty(DATASOURCE_PASSWORD_KEY))) {
            overrides.put(DATASOURCE_PASSWORD_KEY, password);
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
