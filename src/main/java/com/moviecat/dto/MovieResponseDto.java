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
@Schema(name = "MovieResponse", description = "Movie response payload")
public class MovieResponseDto {

    @Schema(description = "Movie ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(description = "Movie title", example = "Interstellar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    @Schema(description = "Release year", example = "2014", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer year;
    @Schema(description = "Duration in minutes", example = "169", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;
    @Schema(description = "Total view count", example = "1200000", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "Average rating based on reviews", example = "8.7")
    private Double averageRating;
    @Schema(description = "Total number of reviews", example = "14", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reviewCount;

    @Schema(description = "Movie genres")
    private List<GenreItemDto> genres;
}
