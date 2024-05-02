package com.example.blog.controller;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.repository.BlogUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final BlogUserRepository blogUserRepository;


    @ModelAttribute("profilePath")
    public String currentUser(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return "none";
        }
        Optional<BlogUser> user = blogUserRepository.findByUsername(currentUser.getUsername());
        if(user.isPresent()) {
            return user.get().getProfilePicturePath(); // 현재 로그인한 사용자 정보
        }
        return "none";
    }

}