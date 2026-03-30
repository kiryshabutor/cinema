package com.moviecat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movieCatOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MovieCat API")
                        .description("REST API for movie catalog management: CRUD, advanced search, caching, "
                                + "error handling, and API documentation")
                        .version("v1")
                        .contact(new Contact()
                                .name("MovieCat Team")
                                .email("moviecat@local.dev"))
                        .license(new License().name("Educational Use")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local environment"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Alternative local environment")))
                .tags(List.of(
                        new Tag().name("Movies").description("Movie management and advanced search"),
                        new Tag().name("Directors").description("Director management"),
                        new Tag().name("Genres").description("Genre management"),
                        new Tag().name("Studios").description("Studio management"),
                        new Tag().name("Reviews").description("Review management"),
                        new Tag().name("Tasks").description("Asynchronous task management")));
    }
}
