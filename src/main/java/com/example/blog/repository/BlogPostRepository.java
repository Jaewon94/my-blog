package com.example.blog.repository;

import com.example.blog.domain.entity.BlogPost;
import com.example.blog.domain.entity.BlogUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Page<BlogPost> findByPostTitleContainingIgnoreCase(String keyword, Pageable pageable);

    List<BlogPost> findTop9ByOrderByPostCreatedAtDesc();

    // 댓글이 많은 순으로 상위 5개 게시글을 조회
    @Query("SELECT p FROM BlogPost p ORDER BY size(p.postComments) DESC")
    List<BlogPost> findTopPostsByComments(Pageable pageable);
    @Query("SELECT new map(MONTH(p.postCreatedAt) AS month, COUNT(p) AS count) " +
            "FROM BlogPost p WHERE YEAR(p.postCreatedAt) = :year GROUP BY MONTH(p.postCreatedAt)")
    List<Map<String, Object>> countNewPostsByMonth(@Param("year") int year);

    @Query("SELECT p.imagePaths FROM BlogPost p WHERE p.postId = :postId")
    List<String> findImagePathsByPostId(@Param("postId") Long postId);
}
