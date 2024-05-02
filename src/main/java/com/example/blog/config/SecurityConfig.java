package com.example.blog.config;

import com.example.blog.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 요청에 대한 인증 규칙 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**", "/home","/temp/**", "/h2-console/**", "/imgs/**","/register", "/register/verify/**", "/css/**", "/js/**",  "/profile","/posts/**"
                        ,"/find-id/**","/find-password/**","/reset/**","/reset-password/**").permitAll() // 특정 경로 인증 없이 접근 허용
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // "/admin/**" 경로는 "ROLE_ADMIN" 권한을 가진 사용자만 접근 가능
                        .anyRequest().authenticated() // 나머지 요청에 대해서는 인증 요구
                )
                .csrf().disable() // CSRF 보호 비활성화 (개발 모드에서만 사용)
                .headers().frameOptions().disable() // H2 콘솔의 프레임 옵션 비활성화
                .and()
                .formLogin()
                    .loginPage("/login") // 사용자 정의 로그인 페이지 경로
                    .loginProcessingUrl("/perform_login")
                    .defaultSuccessUrl("/home", true) // 로그인 성공 시 리다이렉트할 기본 URL
                    .failureUrl("/login?error=true")
                    .successHandler(new CustomAuthenticationSuccessHandler())
                    .failureHandler(new CustomAuthenticationFailureHandler())
                    .permitAll() // 로그인 페이지는 인증 없이 접근 허용
                .and()
                .logout()
                    .logoutSuccessUrl("/login?logout") // 로그아웃 성공 시 리다이렉트할 URL
                    .permitAll(); // 로그아웃 기능 역시 인증 없이 접근 허용

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


}
