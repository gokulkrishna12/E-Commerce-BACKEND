package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.UserProfileResponse;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return new UserProfileResponse(user.getName(), user.getEmail(), user.getRole());
    }

    public UserProfileResponse updateProfile(String email, String newName) {
        if (newName == null || newName.trim().length() < 3) {
            throw new RuntimeException("Name must be at least 3 characters!");
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setName(newName.trim());
        userRepository.save(user);
        return new UserProfileResponse(user.getName(), user.getEmail(), user.getRole());
    }

    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect!");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}