package com.example.blog.controller;

import com.example.blog.domain.dto.BlogCommentDTO;
import com.example.blog.domain.entity.BlogComment;
import com.example.blog.service.BlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/temp")
@RequiredArgsConstructor
public class BlogCommentRestController {

    private final BlogCommentService blogCommentService;

    // 댓글 삭제
    @DeleteMapping("/comments/{id}/delete")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        blogCommentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    // 댓글 수정
    @PostMapping("/comments/{id}/edit")
    public ResponseEntity<BlogComment> updateComment(@PathVariable Long id, @RequestBody BlogCommentDTO commentDTO) {
        BlogComment updatedComment = blogCommentService.updateComment(id, commentDTO);
        return ResponseEntity.ok(updatedComment);
    }
}
