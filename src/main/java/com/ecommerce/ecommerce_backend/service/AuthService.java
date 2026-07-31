package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.*;
import com.ecommerce.ecommerce_backend.entity.RefreshToken;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(auth);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken(), user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(auth);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken(), user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found. Please login again."));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(user.getEmail());

        return new AuthResponse(newAccessToken, refreshToken.getToken(), user.getEmail(), user.getName(), user.getRole());
    }

    public void logout(String requestRefreshToken) {
        refreshTokenService.deleteByToken(requestRefreshToken);
    }
}