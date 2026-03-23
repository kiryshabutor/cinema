package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Schema(name = "ReviewCreateItem", description = "Review item for bulk creation")
public class ReviewCreateItemDto extends ReviewContentDto {

    public ReviewCreateItemDto(String authorAlias, Integer rating, String comment) {
        super(authorAlias, rating, comment);
    }
}
