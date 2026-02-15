package com.moviecat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudioDto {
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String address;
}
