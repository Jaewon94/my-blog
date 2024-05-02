package com.example.blog.service;

import com.example.blog.domain.dto.BlogCommentDTO;
import com.example.blog.domain.entity.BlogComment;
import com.example.blog.domain.entity.BlogPost;
import com.example.blog.repository.BlogCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogCommentService {

    private final BlogCommentRepository blogCommentRepository;


    public void deleteComment(Long id) {
        BlogComment comment = blogCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invalid post Id: " + id));

        blogCommentRepository.delete(comment);
    }

    public BlogComment updateComment(Long id, BlogCommentDTO commentDTO) {
        // 댓글 찾기
        BlogComment comment = blogCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));

        // 댓글 내용 업데이트
        comment.setCommentContent(commentDTO.getContent());

        // 업데이트된 댓글 저장 및 반환
        return blogCommentRepository.save(comment);
    }

    // 댓글 페이징
    public Page<BlogComment> findCommentsByPostId(BlogPost commentPost, Pageable pageable) {
        return blogCommentRepository.findByCommentPost(commentPost, pageable);
    }
}
