package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Movie response model")
public class MovieResponseDto {

    @Schema(description = "Movie ID", example = "1")
    private Long id;
    @Schema(description = "Movie title", example = "Interstellar")
    private String title;
    @Schema(description = "Release year", example = "2014")
    private Integer year;
    @Schema(description = "Duration in minutes", example = "169")
    private Integer duration;
    @Schema(description = "Total view count", example = "1200000")
    private Long viewCount;
    @Schema(description = "Poster URL", example = "/uploads/abc123.jpg")
    private String posterUrl;

    @Schema(description = "Director ID", example = "1")
    private Long directorId;
    @Schema(description = "Director last name", example = "Nolan")
    private String directorLastName;
    @Schema(description = "Director first name", example = "Christopher")
    private String directorFirstName;
    @Schema(description = "Director middle name")
    private String directorMiddleName;

    @Schema(description = "Studio ID", example = "2")
    private Long studioId;
    @Schema(description = "Studio title", example = "Warner Bros")
    private String studioTitle;

    @Schema(description = "Movie genres")
    private List<GenreItemDto> genres;
}
