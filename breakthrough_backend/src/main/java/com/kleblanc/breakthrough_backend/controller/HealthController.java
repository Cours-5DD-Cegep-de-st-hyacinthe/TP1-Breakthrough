package com.kleblanc.breakthrough_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
public class HealthController {

    @GetMapping("/")
    public String index() {
        return "Server up!";
    }

    // Create a session and store an attribute
    @GetMapping("/create")
    public String createSession(HttpSession session, HttpServletRequest request) {
        // Set a session attribute (e.g., username)
        session.setAttribute("username", "JohnDoe");

        // Retrieve and return the session ID
        String sessionId = session.getId();
        return "Session created with ID: " + sessionId;
    }

    // Retrieve session attribute
    @GetMapping("/get")
    public String getSession(HttpSession session) {
        // Get the session attribute (username)
        String username = (String) session.getAttribute("username");

        // If no session exists, return an error message
        if (username == null) {
            return "No session found!";
        }

        // Return session data to the client
        return "Session found with username: " + username;
    }

    // Invalidate the session
    @GetMapping("/invalidate")
    public String invalidateSession(HttpSession session) {
        // Invalidate the session
        session.invalidate();
        return "Session invalidated!";
    }
}
