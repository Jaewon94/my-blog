package com.example.blog.service;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.repository.BlogUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final BlogUserRepository blogUserRepository;
    private final PasswordEncoder passwordEncoder;

//    /**
//     * 사용자 회원가입 처리.
//     */
//    @Transactional
//    public void registerUser(BlogUser user) {
//        // 비밀번호 해싱
//        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
//
//        // 기본 사용자 활성화 상태는 false
//        user.setActive(false);
//
//        // 인증 코드 생성 및 만료 시간 설정
//        String verificationCode = UUID.randomUUID().toString();
//        user.setVerificationCode(verificationCode);
//        user.setVerificationCodeExpiryDate(LocalDateTime.now().plusHours(24)); // 24시간 후 만료
//
//        // 사용자 정보 저장
//        blogUserRepository.save(user);
//
//        // 인증 이메일 발송
//        sendVerificationEmail(user);
//    }

    /**
     * 인증 이메일 발송.
     */
    public void sendVerificationEmail(BlogUser user) {
        String subject = "Please verify your email";
        String confirmationUrl = "http://localhost:8080/register/verify?code=" + user.getVerificationCode();
        String message = "Thank you for registering. Please click the link below to verify your email address: " + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getUserEmail());
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    /**
     * 이메일 인증 코드 검증 및 계정 활성화.
     */
    @Transactional
    public boolean verifyEmail(String verificationCode) {
        System.out.println("verificationCode = " + verificationCode);
        BlogUser user = blogUserRepository.findByVerificationCode(verificationCode);
        if (user != null && !user.isActive() && user.getVerificationCodeExpiryDate().isAfter(LocalDateTime.now())) {
            user.setActive(true);
            user.setVerificationCode(null); // 인증 완료 후 코드 초기화
            blogUserRepository.save(user);
            return true;
        }
        return false;
    }

    public void sendIdByEmail(String to, String userId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your User ID");
        message.setText("Your User ID is: " + userId);
        mailSender.send(message);
    }

    /**
     * 사용자에게 이메일을 발송하는 메서드.
     *
     * @param userEmail 사용자 이메일 주소.
     * @param subject 이메일 제목.
     * @param text 이메일 본문.
     */
    public void sendSimpleMessage(String userEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
