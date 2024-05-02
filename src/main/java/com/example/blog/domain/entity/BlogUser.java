package com.example.blog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "blog_users")
@Getter @Setter
public class BlogUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String username;
    private String passwordHash; // 비밀번호 필드명 변경
    private String userEmail; // 이메일 필드명 변경
    private boolean isActive = false; // 필드명 변경 및 이메일 인증 여부

    private String verificationCode;
    private LocalDateTime verificationCodeExpiryDate;

    // 프로필 사진 URL을 저장할 필드 추가
    @Column(length = 1000)
    private String profilePicturePath = "/profile/basic-profile.jpeg";


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> userRoles = new HashSet<>();

    // 사용자 생성 날짜
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

}
