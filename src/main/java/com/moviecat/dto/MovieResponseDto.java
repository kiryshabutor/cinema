package com.moviecat.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {

    private Long id;
    private String title;
    private Integer year;
    private Integer duration;
    private Long viewCount;
    private String posterUrl;

    private Long directorId;
    private String directorLastName;
    private String directorFirstName;
    private String directorMiddleName;

    private Long studioId;
    private String studioTitle;

    private List<GenreItemDto> genres;
}
