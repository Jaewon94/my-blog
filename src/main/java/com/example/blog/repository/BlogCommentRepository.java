package com.example.blog.repository;

import com.example.blog.domain.entity.BlogComment;
import com.example.blog.domain.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    Page<BlogComment> findByCommentPost(BlogPost commentPost, Pageable pageable);

}
