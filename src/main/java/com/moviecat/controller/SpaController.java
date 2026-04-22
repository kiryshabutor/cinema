package com.moviecat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/app", "/app/"})
    public String forwardToSpaIndex() {
        return "forward:/app/index.html";
    }

    @GetMapping("/")
    public String redirectRootToSpa() {
        return "redirect:/app/";
    }
}
