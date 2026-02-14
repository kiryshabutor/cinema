package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {  

    private Long id;
    private String title;
    private Integer year;
    private Integer duration;
    private Long viewCount;
}
