package com.kleblanc.breakthrough_backend.controller;

import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class HealthController {
    @GetMapping("/")
    public String index() {
        return "Server up!";
    }
}
