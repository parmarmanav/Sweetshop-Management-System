package com.sweetshop.sweetshop_backend.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.sweetshop.sweetshop_backend.model.User;
import com.sweetshop.sweetshop_backend.repository.UserRepository;
import com.sweetshop.sweetshop_backend.util.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String loginUser(String username, String password) {
        try {
            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Input username: '" + username + "'");
            System.out.println("Input password: '" + password + "'");
            
            if (username == null || password == null) {
                System.out.println("Username or password is null");
                return null;
            }
            
            Optional<User> userOptional = userRepository.findByUsername(username);
            System.out.println("User found in DB: " + userOptional.isPresent());
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("DB username: '" + user.getUsername() + "'");
                System.out.println("DB password: '" + user.getPassword() + "'");
                System.out.println("DB role: '" + user.getRole() + "'");
                System.out.println("Password match: " + password.equals(user.getPassword()));
                
                if (password.equals(user.getPassword())) {
                    // Include role in token generation
                    String token = jwtUtil.generateTokenWithRole(username, user.getRole());
                    System.out.println("Generated token: " + token);
                    return token;
                }
            } else {
                System.out.println("All users in database:");
                userRepository.findAll().forEach(u -> 
                    System.out.println("- Username: '" + u.getUsername() + "', Role: '" + u.getRole() + "'")
                );
            }
            
            return null;
        } catch (Exception e) {
            System.out.println("Error in loginUser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String registerUser(String username, String password) {
        try {
            System.out.println("=== SIGNUP DEBUG ===");
            System.out.println("Signup username: '" + username + "'");
            System.out.println("Signup password: '" + password + "'");
            
            if (userRepository.existsByUsername(username)) {
                System.out.println("User already exists");
                return "Username already exists";
            }
            
            if (username == null || username.trim().isEmpty()) {
                return "Username cannot be empty";
            }
            
            if (password == null || password.length() < 6) {
                return "Password must be at least 6 characters long";
            }
            
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setPassword(password);
            newUser.setRole("USER"); // Default role for new registrations
            
            User savedUser = userRepository.save(newUser);
            System.out.println("User saved with ID: " + savedUser.getId());
            System.out.println("Saved username: '" + savedUser.getUsername() + "'");
            System.out.println("Saved role: '" + savedUser.getRole() + "'");
            
            return "User registered successfully";
            
        } catch (Exception e) {
            System.out.println("Error in registerUser: " + e.getMessage());
            e.printStackTrace();
            return "Registration failed";
        }
    }
}