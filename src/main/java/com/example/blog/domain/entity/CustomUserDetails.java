package com.example.blog.domain.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isActive;
    @Getter
    private String profilePicturePath;

    // 사용자 정의 UserDetails 생성자
    public CustomUserDetails(String username, String password, String profilePicturePath, Collection<? extends GrantedAuthority> authorities, boolean isActive) {
        this.username = username;
        this.password = password;
        this.profilePicturePath = profilePicturePath;
        this.authorities = authorities;
        this.isActive = isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 혹은 계정 만료 상태를 나타내는 로직
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 혹은 계정 잠김 상태를 나타내는 로직
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 혹은 크리덴셜 만료 상태를 나타내는 로직
    }

    @Override
    public boolean isEnabled() {
        return isActive; // 활성 상태 필드 사용
    }

}
