package com.xxicon.code.reactor.http.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {
    @GetMapping
    public String index() {
        return "Hello!";
    }
}
