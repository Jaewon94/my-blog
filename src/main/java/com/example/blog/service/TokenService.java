package com.example.blog.service;

public interface TokenService {
    String createTokenForUser(String userEmail);
    String validateToken(String token);}
