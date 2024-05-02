package com.example.blog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Getter @Setter
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;
    private String token;
    private LocalDateTime tokenExpiryDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private BlogUser tokenUser;

    public VerificationToken() {
        super();
    }

    public VerificationToken(String token, BlogUser user) {
        super();
        this.token = token;
        this.tokenUser = user;
        this.tokenExpiryDate = calculateExpiryDate(24 * 60); // 24시간 후 만료
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        return LocalDateTime.now().plusMinutes(expiryTimeInMinutes);
    }
}
