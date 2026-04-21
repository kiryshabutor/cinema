package com.moviecat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tmdb")
public class TmdbProperties {

    private String apiKey = "";
    private String apiBaseUrl = "https://api.themoviedb.org/3";
    private String imageBaseUrl = "https://image.tmdb.org/t/p/w500";
    private String language = "en-US";
}
