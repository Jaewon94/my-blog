package com.example.blog.controller;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.repository.BlogUserRepository;
import com.example.blog.service.BlogUserService;
import com.example.blog.service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class VerifyController {

    private final BlogUserService blogUserService;


    @GetMapping("/register/verify")
    public String verifyEmail(@RequestParam("code") String code, Model model) {
        boolean isVerified = blogUserService.verifyUserEmail(code);
        if (isVerified) {
            // 인증 성공 시, 사용자를 로그인 페이지로 리디렉션하고 성공 메시지를 표시합니다.
            model.addAttribute("message", "Your email has been successfully verified. Please log in.");
            return "login";
        } else {
            // 인증 실패 시, 사용자에게 실패 메시지를 표시하고, 다시 이메일 인증을 요청할 수 있는 페이지로 안내합니다.
            model.addAttribute("error", "Verification failed or the code has expired. Please request a new verification email.");
            return "email-verification"; // 실패 시 보여줄 페이지의 이름 (예: email-verification.html)
        }
    }

    @GetMapping("/verify-email-form")
    public String showVerifyEmailForm() {
        return "verify-email-form";
    }


}
