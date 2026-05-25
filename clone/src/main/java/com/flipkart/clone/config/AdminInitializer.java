package com.flipkart.clone.config;

import com.flipkart.clone.entity.User;
import com.flipkart.clone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@flipkart.com").isEmpty()) {

            User admin = new User();

            admin.setName("Admin");

            admin.setEmail("admin@flipkart.com");

            admin.setPasswordHash(
                    passwordEncoder.encode("admin123")
            );

            admin.setIsActive(true);

            admin.setRole(User.Role.ADMIN);

            userRepository.save(admin);

            System.out.println("ADMIN CREATED SUCCESSFULLY");
        }
    }
}