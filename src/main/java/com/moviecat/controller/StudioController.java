package com.moviecat.controller;

import com.moviecat.dto.StudioDto;
import com.moviecat.model.Studio;
import com.moviecat.repository.StudioRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final StudioRepository studioRepository;

    @GetMapping
    public List<StudioDto> getAll() {
        return studioRepository.findAll().stream()
                .map(s -> new StudioDto(s.getId(), s.getTitle(), s.getAddress()))
                .toList();
    }

    @PostMapping
    public StudioDto create(@jakarta.validation.Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio saved = studioRepository.save(studio);
        return new StudioDto(saved.getId(), saved.getTitle(), saved.getAddress());
    }
}
