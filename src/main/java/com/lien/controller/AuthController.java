package com.lien.controller;

import com.lien.entity.User;
import com.lien.service.UserService;
import com.lien.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        String name = req.get("name");
        User user = userService.register(email, password, name);
        return ResponseEntity.ok(Map.of("id", user.getId(), "email", user.getEmail(), "name", user.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        User user = userService.authenticate(email, password);
        String token = JwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of("token", token, "email", user.getEmail(), "name", user.getName()));
    }
} 