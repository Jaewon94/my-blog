package com.example.blog.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.example.blog.domain.entity.BlogComment;
import com.example.blog.domain.entity.BlogPost;
import com.example.blog.domain.entity.BlogUser;
import com.example.blog.service.BlogCommentService;
import com.example.blog.service.BlogPostService;
import com.example.blog.service.BlogUserService;
import com.example.blog.service.S3Service;
import io.awspring.cloud.s3.ObjectMetadata;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/temp")
@RequiredArgsConstructor
public class TempoPostController {

    private final BlogPostService blogPostService;
    private final BlogCommentService blogCommentService;
    private final BlogUserService blogUserService;
    private final S3Service s3Service;

    private static final Logger logger = LoggerFactory.getLogger(TempoPostController.class);


    // 파일 저장 경로, 실제 환경에 맞게 설정
    private final String uploadDir = "src/main/resources/static/upload-dir/";
    private final S3Client s3Client;

    @GetMapping
    public String listPosts(Model model, Principal principal,
                            @PageableDefault(size = 15, sort = "postCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
                            HttpServletRequest request,
                            @RequestParam(name = "keyword", required = false) String keyword) {
        // 페이지네이션 처리
        Page<BlogPost> postsPage;

        if (keyword != null && !keyword.isEmpty()) {
            // 검색어가 있을 경우, 해당 검색어를 포함하는 게시글 조회
            postsPage = blogPostService.searchPostsByKeyword(keyword, pageable);
        } else {
            // 검색어가 없을 경우, 모든 게시글 조회
            postsPage = blogPostService.getAllPosts(pageable);
        }

        List<BlogPost> posts = postsPage.getContent();

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", postsPage.getNumber());
        model.addAttribute("totalPages", postsPage.getTotalPages());

        String currentUsername = principal != null ? principal.getName() : null;
        model.addAttribute("currentUsername", currentUsername);

        model.addAttribute("currentURI", request.getRequestURI());

        return "posts/list"; // 게시글 목록을 보여주는 Thymeleaf 템플릿 경로
    }

    // 게시글 작성 페이지
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new BlogPost());
        return "posts/create"; // 게시글 작성 폼을 보여주는 Thymeleaf 템플릿 경로
    }

    // 게시글 생성 처리
    @PostMapping
    public String createPost(BlogPost post, Principal principal,
                             @RequestParam("postImages") MultipartFile[] images, // 여러 이미지 파일 받기
                             RedirectAttributes redirectAttributes) {

        // Current logged-in user information
        String username = principal.getName();
        BlogUser author = blogUserService.getUserByUsername(username);
        post.setPostAuthor(author);

        List<String> imageUrls = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String ymd = dateFormat.format(new Date());
                String fileName = UUID.randomUUID().toString();
                String ext = file.getContentType().substring(file.getContentType().indexOf("/") + 1);
                String key = "postImage/" + ymd + "_" + fileName + "." + ext;

                try {
                    // Upload file to S3 and retrieve the URL
                    s3Service.uploadFile(file, key); // Assuming 'uploadFile' needs the file and key
                    String fileUrl = s3Service.getFileUrl(key);
                    imageUrls.add(fileUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("message", "Image upload failed: " + e.getMessage());
                    return "redirect:/temp";
                }
            }
        }

        // Save image paths to the post
        post.setImagePaths(imageUrls);

        // Create the post
        blogPostService.createPost(post);

        redirectAttributes.addFlashAttribute("message", "Post created successfully.");
        return "redirect:/temp";
    }


    // 게시글 수정 페이지
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        BlogPost post = blogPostService.getPostById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));
        model.addAttribute("post", post);
        return "posts/edit"; // 게시글 수정 폼을 보여주는 Thymeleaf 템플릿 경로
    }

    // 게시글 수정 처리
    @PostMapping("/{id}/edit")
    @Transactional
    public String updatePost(@PathVariable Long id, BlogPost post, RedirectAttributes redirectAttributes, @RequestParam("photos") MultipartFile[] newPhotos) {
        // Fetch the current photo keys associated with the post
        List<String> oldPhotoUrls = blogPostService.getPhotoUrlsByPostId(id); // Make sure this method returns URLs


        // Log the old URLs
        logger.info("Old Photo URLs: {}", oldPhotoUrls);


        // Upload new photos to S3 and collect their keys
        List<String> newPhotoKeys = Arrays.stream(newPhotos)
                .map(this::uploadPhotoToS3)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Log the new keys
        logger.info("New Photo Keys: {}", newPhotoKeys);

        // Update the post with the new photo keys and other post data
        post.setImagePaths(newPhotoKeys);
        blogPostService.updatePost(id, post);

        // Add a success message to the redirect attributes
        redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 수정되었습니다.");

        // Delete old photos from S3
        deletePhotosFromS3(oldPhotoUrls, newPhotoKeys);

        blogPostService.updatePost(id, post);

        return "redirect:/temp";
    }



    private String uploadPhotoToS3(MultipartFile photo) {
        String key = generateUniqueKeyForPhoto(photo);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("thisismyblogbucket")
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(photo.getInputStream(), photo.getSize()));

            // Generate the public URL
            return generatePublicUrl("thisismyblogbucket", key);
        } catch (IOException e) {
            logger.error("Error uploading photo: {}", e.getMessage(), e);
            return null;
        }
    }

    private String generatePublicUrl(String bucketName, String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    private void deletePhotosFromS3(List<String> oldUrls, List<String> newKeys) {
        List<String> oldKeys = oldUrls.stream()
                .map(this::extractKeyFromUrl)
                .collect(Collectors.toList());

        oldKeys.stream()
                .filter(key -> !newKeys.contains(key))
                .forEach(key -> {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket("thisismyblogbucket")
                                .key(key)
                                .build());
                        logger.info("Successfully deleted object: {}", key);
                    } catch (Exception e) {
                        logger.error("Failed to delete object: {} due to: {}", key, e.getMessage(), e);
                    }
                });
    }

    private String extractKeyFromUrl(String url) {
        try {
            URL uri = new URL(url);
            String key = uri.getPath().substring(1); // Remove the leading '/' from the path
            return key;
        } catch (MalformedURLException e) {
            logger.error("Invalid URL format: {}", url, e);
            return null;
        }
    }

    private String generateUniqueKeyForPhoto(MultipartFile photo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String ymd = dateFormat.format(new Date());
        String fileName = UUID.randomUUID().toString();
        String ext = photo.getContentType().substring(photo.getContentType().indexOf("/") + 1);
        return "postImage/" + ymd + "_" + fileName + "." + ext;
    }


    // 게시글 삭제 처리
    @GetMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try{
            blogPostService.deletePost(id);
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting the post.");
            logger.error("Error deleting post with id: {}", id, e);
        }

        return "redirect:/temp";
    }

    // 게시글 상세보기 페이지
    @GetMapping("/{id}")
    public String showPostDetail(@PathVariable Long id, Model model, Principal principal, @PageableDefault(size = 5) Pageable pageable) {
        BlogPost post = blogPostService.getPostById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + id));

        model.addAttribute("post",post);
        // 현재 로그인한 사용자 정보를 추가
        String currentUsername = principal != null ? principal.getName() : null;
        model.addAttribute("currentUsername", currentUsername);

        // 댓글 페이징 처리
        Page<BlogComment> commentsPage = blogCommentService.findCommentsByPostId(post, pageable);
        model.addAttribute("commentsPage", commentsPage);

        // 페이징 처리를 위한 추가 정보
        model.addAttribute("pageNumbers", IntStream.range(0, commentsPage.getTotalPages())
                .boxed()
                .collect(Collectors.toList()));

        return "posts/detail";
    }

    // 댓글 추가 처리
    @PostMapping("/{postId}/comments")
    public String addComment(@PathVariable Long postId, String content, Principal principal) {
        // 여기서는 사용자 인증 정보를 Principal 객체에서 받아와 사용하고 있습니다.
        // 실제 구현에서는 사용자 정보를 가져오는 방식에 따라 다를 수 있습니다.
        String username = principal.getName(); // 가정
        blogPostService.addCommentToPost(postId, content, username);
        return "redirect:/temp/" + postId;
    }



}
