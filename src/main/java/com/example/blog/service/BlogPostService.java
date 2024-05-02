package com.example.blog.service;

import com.amazonaws.AmazonServiceException;
import com.example.blog.domain.entity.BlogComment;
import com.example.blog.domain.entity.BlogPost;
import com.example.blog.domain.entity.BlogUser;
import com.example.blog.repository.BlogCommentRepository;
import com.example.blog.repository.BlogPostRepository;
import com.example.blog.repository.BlogUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final BlogUserRepository blogUserRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final S3Client s3Client;
    private static final Logger logger = LoggerFactory.getLogger(BlogPostService.class);


    @Transactional
    public BlogPost createPost(BlogPost post) {
        // 현재 인증된 사용자의 UserDetails 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        // 사용자 이름을 사용하여 BlogUser 객체 찾기
        // 이 예시에서는 사용자 서비스 또는 리포지토리가 userDetailsService라고 가정합니다.
        // 실제로는 해당 서비스/리포지토리의 메소드를 호출하여 BlogUser 객체를 가져와야 합니다.
        Optional<BlogUser> user = blogUserRepository.findByUsername(username);

        // BlogPost 객체에 작성자 정보 설정
        post.setPostAuthor(user.get());

        // 게시글 저장
        return blogPostRepository.save(post);
    }

    // 게시글 조회 (단일)
    public Optional<BlogPost> getPostById(Long id) {
        return blogPostRepository.findById(id);
    }

    // 게시글 조회 (전체)
    public Page<BlogPost> getAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    // 게시글 수정
    @Transactional
    public BlogPost updatePost(Long id, BlogPost updatedPost) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));
        post.setPostTitle(updatedPost.getPostTitle());
        post.setPostContent(updatedPost.getPostContent());
        post.setImagePaths(updatedPost.getImagePaths());
        return post;
    }

    // 게시글 삭제
    public void deletePost(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));

        // Delete photos from S3
        List<String> photoUrls = post.getImagePaths();  // This should get URLs directly
        for (String urlString : photoUrls) {
            try {
                URL url = new URL(urlString);
                String key = url.getPath().substring(1); // Remove the leading '/' from the path

                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket("thisismyblogbucket")
                        .key(key)
                        .build());
                logger.info("Successfully deleted object: {}", key);
            } catch (Exception e) {
                logger.error("Failed to delete S3 object from URL: {}", urlString, e);
            }
        }

        // Delete the post
        blogPostRepository.delete(post);
    }

    // 게시글에 댓글 추가
    public void addCommentToPost(Long postId, String content, String username) {
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));
        BlogUser user = blogUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        BlogComment comment = new BlogComment();
        comment.setCommentContent(content);
        comment.setCommentPost(post);
        comment.setCommentAuthor(user);
        blogCommentRepository.save(comment);
    }


    public Page<BlogPost> searchPostsByKeyword(String keyword, Pageable pageable) {
        return blogPostRepository.findByPostTitleContainingIgnoreCase(keyword, pageable);
    }


    public List<BlogPost> findLatest9Posts() {
        return blogPostRepository.findTop9ByOrderByPostCreatedAtDesc();

    }

    // 댓글이 많은 게시글 상위 5개 조회
    public List<BlogPost> findTopPostsByComments() {
        return blogPostRepository.findTopPostsByComments(PageRequest.of(0, 5));
    }

    public void deletePostById(Long postId) {
        blogPostRepository.deleteById(postId);
    }

    public List<Map<String, Object>> countNewPostsByMonth(int currentYear) {
        return blogPostRepository.countNewPostsByMonth(currentYear);
    }

    public List<String> getPhotoUrlsByPostId(Long postId) {
        return blogPostRepository.findImagePathsByPostId(postId);
    }
}
