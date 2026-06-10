package com.sweetshop.sweetshop_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sweetshop.sweetshop_backend.repository.UserRepository;
import com.sweetshop.sweetshop_backend.util.JwtUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/check")
    public ResponseEntity<String> checkAdminStatus(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("No token provided");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if ("ADMIN".equals(role)) {
                return ResponseEntity.ok("Admin access confirmed for user: " + username);
            } else {
                return ResponseEntity.status(403).body("Access denied. Admin role required.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<String> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("No token provided");
            }

            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Access denied. Admin role required.");
            }

            StringBuilder result = new StringBuilder();
            result.append("Total users: ").append(userRepository.count()).append("\n");
            userRepository.findAll().forEach(user -> 
                result.append("Username: ").append(user.getUsername())
                      .append(", Role: ").append(user.getRole()).append("\n")
            );
            
            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}