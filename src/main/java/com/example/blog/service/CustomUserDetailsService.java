package com.example.blog.service;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.domain.entity.CustomUserDetails;
import com.example.blog.repository.BlogUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final BlogUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BlogUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        System.out.println("user.getPasswordHash() = " + user.getPasswordHash());

        // 권한 목록을 GrantedAuthority 객체 목록으로 변환
        Collection<? extends GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // CustomUserDetails 객체 생성 및 반환
        return new CustomUserDetails(
                user.getUsername(),
                user.getPasswordHash(),
                user.getProfilePicturePath(),
                authorities,
                user.isActive()
        );
    }
}
