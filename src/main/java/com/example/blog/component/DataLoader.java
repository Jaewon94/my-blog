package com.example.blog.component;

import com.example.blog.domain.entity.BlogUser;
import com.example.blog.domain.entity.Role;
import com.example.blog.repository.BlogUserRepository;
import com.example.blog.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final BlogUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Override
    public void run(String... args) throws Exception {
        // 기본 역할 생성
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.saveAndFlush(new Role("ROLE_USER")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.saveAndFlush(new Role("ROLE_ADMIN")));

        // admin 사용자 생성
        String adminUsername = "admin";
        Optional<BlogUser> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isEmpty()) {
            BlogUser adminUser = new BlogUser();
            adminUser.setUsername(adminUsername);
            adminUser.setPasswordHash(passwordEncoder.encode("admin")); // 예시 비밀번호, 실제 사용 시 보안 강력한 비밀번호 사용
            adminUser.setActive(true);

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole); // admin에게는 USER와 ADMIN 역할 모두 부여
            adminUser.setUserRoles(adminRoles);

            userRepository.saveAndFlush(adminUser);
        }
    }
}
