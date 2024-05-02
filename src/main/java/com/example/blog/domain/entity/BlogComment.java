package com.example.blog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_comments")
@Getter @Setter
public class BlogComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    private String commentContent;
    private LocalDateTime commentCreatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "post_id")
    private BlogPost commentPost;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private BlogUser commentAuthor;
}
