package com.example.blog.controller;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.service.BlogUserService;
import com.example.blog.service.S3Service;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final BlogUserService blogUserService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private static final String UPLOADED_FOLDER = "src/main/resources/static/profile/";



    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        String username = principal.getName();
        BlogUser user = blogUserService.findByUsername(username); // 사용자 이름으로 사용자 정보 조회
        model.addAttribute("user", user);
        return "profile";
    }

    // 프로필 업데이트 메서드
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("profilePicture") MultipartFile profilePicture,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        String username = principal.getName();

        try {
            // 프로필 사진 업로드 로직
            if (!profilePicture.isEmpty()) {
                String profilePicturePath = saveProfilePicture(profilePicture, username);
                blogUserService.updateProfilePicture(username, profilePicturePath);
            }

            // 비밀번호 변경 로직
            if (!newPassword.isEmpty()) {
                if(newPassword.equals(confirmPassword)) {
                    blogUserService.updatePassword(username, newPassword);
                    redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("passwordError", "Passwords do not match.");
                    return "redirect:/profile";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the profile.");
            return "redirect:/profile";
        }

        return "redirect:/";
    }

    private String saveProfilePicture(MultipartFile profilePicture, String username) throws IOException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        if (!profilePicture.isEmpty()) {
            String ymd = dateFormat.format(new Date());
            String fileName = UUID.randomUUID().toString();
            String ext = profilePicture.getContentType().substring(profilePicture.getContentType().indexOf("/") + 1);
            String key = "profileImg/" + ymd + "_" + fileName + "." + ext;

            try {
                // Upload file to S3 and retrieve the URL
                s3Service.uploadFile(profilePicture, key); // Assuming 'uploadFile' needs the file and key
                String fileUrl = s3Service.getFileUrl(key);
                System.out.println("fileUrl = " + fileUrl);
                return fileUrl;
            } catch (Exception e) {
                e.printStackTrace();
                return "/profile/basic-profile.jpeg";
            }
        }
        return "/profile/basic-profile.jpeg";
    }
}
