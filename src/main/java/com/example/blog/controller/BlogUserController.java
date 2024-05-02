package com.example.blog.controller;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.exception.UsernameExistsException;
import com.example.blog.service.BlogUserService;
import com.example.blog.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BlogUserController {

    private final EmailService emailService;
    private final BlogUserService blogUserService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/user/profile")
    public String userProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        model.addAttribute("username", currentUser.getUsername());
        return "profile"; // 사용자 프로필 페이지
    }

    @GetMapping("/register")
    public String registerForm(Model model){
        model.addAttribute("blogUser", new BlogUser());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(BlogUser user,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        // 비밀번호와 비밀번호 확인 필드가 일치하지 않는 경우
        if (!user.getPasswordHash().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register";
        }

        // 이메일이 이미 사용 중인지 검사
        if (blogUserService.checkIfEmailExists(user.getUserEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already in use.");
            return "redirect:/register";
        }

        try {
            BlogUser registeredUser = blogUserService.registerUser(user);
            emailService.sendVerificationEmail(registeredUser);
            redirectAttributes.addFlashAttribute("message", "User registered successfully. Please verify your email.");
            return "redirect:/";
        } catch (UsernameExistsException e) {
            // 사용자 이름이 이미 존재하는 경우
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register"; // 사용자 등록 폼으로 리다이렉트
        }
    }



    @GetMapping("/find-id")
    public String findId() {
        return "find-id";
    }

    @PostMapping("/find-id")
    public String processFindIdForm(@RequestParam String email, Model model) {
        String username = blogUserService.findUsernameByUserEmail(email);
        if(username != null) {
            emailService.sendIdByEmail(email, username);
            model.addAttribute("message", "사용자 username이 이메일로 발송되었습니다.");
            return "find-id-success"; // 성공 메시지를 보여주는 뷰
        } else {
            model.addAttribute("error", "제공된 이메일에 대한 사용자 username을 찾을 수 없습니다.");
            return "find-id";
        }
    }

    @GetMapping("/find-password")
    public String findPassword() {
        return "find-password";
    }

    @PostMapping("/reset")
    public String handlePasswordResetRequest(@RequestParam("email") String userEmail, Model model) {
        // 사용자 이메일 확인 로직
        boolean userExists = blogUserService.checkUserExistsByUserEmail(userEmail);

        if (userExists) {
            // 비밀번호 재설정 링크 생성 및 이메일 발송 로직
            blogUserService.sendPasswordResetLink(userEmail);
            model.addAttribute("message", "비밀번호 재설정 링크가 이메일로 발송되었습니다.");
            return "password-reset-requested";
        } else {
            model.addAttribute("error", "해당 이메일 주소를 가진 사용자를 찾을 수 없습니다.");
            return "find-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetUserPassword(@RequestParam("token") String token, Model model){
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetUserPassword(@RequestParam("token") String token, @RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "reset-password";
        }

        boolean resetResult = blogUserService.resetPassword(token, password);

        if (resetResult) {
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "reset-password";
        }
    }
}
