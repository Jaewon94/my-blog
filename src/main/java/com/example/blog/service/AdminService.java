package com.example.blog.service;

import com.example.blog.repository.BlogCommentRepository;
import com.example.blog.repository.BlogPostRepository;
import com.example.blog.repository.BlogUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BlogUserRepository blogUserRepository;
    private final BlogPostRepository blogPostRepository;
    private final BlogCommentRepository blogCommentRepository;

    public long getTotalUsers() {
        return blogUserRepository.count();
    }

    public long getTotalPosts() {
        return blogPostRepository.count();
    }

    public long getTotalComments() {
        return blogCommentRepository.count();
    }
}
