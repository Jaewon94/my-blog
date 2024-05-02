package com.example.blog.service;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.domain.entity.Role;
import com.example.blog.exception.UsernameExistsException;
import com.example.blog.repository.BlogUserRepository;
import com.example.blog.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogUserService {

    private final BlogUserRepository blogUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    public boolean verifyUserEmail(String code) {
        BlogUser user = blogUserRepository.findByVerificationCode(code);

        if (user != null && user.getVerificationCodeExpiryDate().isAfter(LocalDateTime.now()) && !user.isActive()) {
            user.setActive(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiryDate(null);
            blogUserRepository.save(user);
            return true;
        }
        return false;
    }

    public BlogUser findByUsername(String username) {
        return blogUserRepository.findByUsername(username).get();
    }

    // 프로필 사진 경로 업데이트 메소드
    @Transactional
    public void updateProfilePicture(String username, String profilePicturePath) {
        BlogUser user = blogUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        user.setProfilePicturePath(profilePicturePath);
        blogUserRepository.save(user);
    }

    // 비밀번호 업데이트 메소드
    @Transactional
    public void updatePassword(String username, String newPassword) {
        BlogUser user = blogUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        blogUserRepository.save(user);
    }

    // 권한 설정 및 사용자 등록
    public BlogUser registerUser(BlogUser user) throws UsernameExistsException {

        // 사용자 이름 중복 확인
        Optional<BlogUser> existingUser = blogUserRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new UsernameExistsException("The username already exists: " + user.getUsername());
        }

        // 비밀번호 암호화
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // 기본 권한 할당
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.getUserRoles().add(userRole);

        // 기본 사용자 활성화 상태는 false
        user.setActive(false);

        // 인증 코드 생성 및 만료 시간 설정
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiryDate(LocalDateTime.now().plusHours(24)); // 24시간 후 만료

        return blogUserRepository.save(user);
    }

    public BlogUser getUserByUsername(String username) {
        return blogUserRepository.findByUsername(username).get();
    }

    public List<BlogUser> findAllNonAdminUsers() {
        // Fetch all users
        List<BlogUser> users = blogUserRepository.findAll();
        // Filter out administrators
        return users.stream()
                .filter(user -> user.getUserRoles().stream()
                        .noneMatch(role -> "ROLE_ADMIN".equals(role.getName())))
                .collect(Collectors.toList());
    }

    public void getDeleteUser(Long id) {
        blogUserRepository.deleteById(id);

    }

    public List<Map<String, Object>> countNewUsersByMonth(int currentYear) {
        return blogUserRepository.countNewUsersByMonth(currentYear);    }

    public String findUsernameByUserEmail(String email) {
        BlogUser user = blogUserRepository.findUsernameByUserEmail(email);
        return user.getUsername();
    }

    public boolean checkIfEmailExists(String email) {
        // 데이터베이스에서 이메일 주소 검색 로직 구현
        // 이메일 주소가 존재하면 true, 그렇지 않으면 false 반환
        return blogUserRepository.findByUserEmail(email).isPresent();
    }


    public boolean checkUserExistsByUserEmail(String userEmail) {
        return blogUserRepository.existsByUserEmail(userEmail);
    }

    public void sendPasswordResetLink(String userEmail) {
        // 사용자 식별 정보 및 토큰 생성
        String token = tokenService.createTokenForUser(userEmail);

        // 비밀번호 재설정 링크 생성
        String resetLink = "localhost:8080/reset-password?token=" + token;

        // 이메일로 비밀번호 재설정 링크 발송
        emailService.sendSimpleMessage(userEmail, "비밀번호 재설정", "비밀번호를 재설정하려면 다음 링크를 사용하세요: " + resetLink);
    }

    public boolean resetPassword(String token, String newPassword) {
        // 토큰 유효성 검증 및 사용자 식별
        String userEmail = tokenService.validateToken(token);
        if (userEmail == null) {
            return false;
        }

        // 비밀번호 업데이트 로직 (예: 사용자 모델 업데이트 및 저장)
        Optional<BlogUser> user = blogUserRepository.findByUserEmail(userEmail);
        if (user.isPresent()) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.get().setPasswordHash(encodedPassword);
            blogUserRepository.save(user.get());
            return true;
        }

        return false;
    }
}
