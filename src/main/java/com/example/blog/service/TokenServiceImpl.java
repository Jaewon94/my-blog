package com.example.blog.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService{
    private final static Map<String, String> tokenStorage = new HashMap<>();

    @Override
    public String createTokenForUser(String userEmail) {
        String token = UUID.randomUUID().toString();
        // 토큰과 사용자 이메일을 맵핑하여 저장합니다.
        // 실제 애플리케이션에서는 보다 안전한 저장 방식을 고려해야 합니다.
        tokenStorage.put(token, userEmail);
        return token;
    }

    @Override
    public String validateToken(String token) {
        String email = tokenStorage.getOrDefault(token, null); // 토큰이 유효하면 사용자 이메일 반환, 그렇지 않으면 null 반환
        System.out.println("tokenStorage = " + tokenStorage);
        System.out.println("email = " + email);

        return email;
    }
}
