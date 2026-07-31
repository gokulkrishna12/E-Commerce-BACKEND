package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.UpdatePasswordRequest;
import com.ecommerce.ecommerce_backend.dto.UpdateProfileRequest;
import com.ecommerce.ecommerce_backend.dto.UserProfileResponse;
import com.ecommerce.ecommerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                             @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request.getName()));
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully!");
    }
}