package com.to.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartController {

    @GetMapping
    public String greeting() {
        return "StartController greeting!";
    }
}

