package com.ecommerce.ecommerce_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("password")
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;
}