package com.sweetshop.sweetshop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.sweetshop.sweetshop_backend.model.User;
import com.sweetshop.sweetshop_backend.repository.UserRepository;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if it doesn't exist
        if (userRepository.findByUsername("kashyap").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("kashyap");
            adminUser.setPassword("patel123");
            adminUser.setRole("ADMIN");
            userRepository.save(adminUser);
        } else {
            System.out.println("ℹ️  Admin user 'kashyap' already exists");
        }
    }
}