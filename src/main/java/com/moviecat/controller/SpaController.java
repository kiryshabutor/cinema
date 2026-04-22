package com.moviecat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping("/")
    public String redirectRootToSwagger() {
        return "redirect:/swagger-ui.html";
    }
}
