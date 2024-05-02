package com.example.blog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "blog_posts")
@Getter @Setter
@ToString
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    private String postTitle;
    @Lob
    private String postContent;
    private LocalDateTime postCreatedAt = LocalDateTime.now();
    private LocalDateTime postUpdatedAt = LocalDateTime.now();

    @ElementCollection // 1
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id")) // 2
    @Column(name = "image_path", length = 1000) // 3
    private List<String> imagePaths = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private BlogUser postAuthor;

    @OneToMany(mappedBy = "commentPost", cascade = CascadeType.ALL)
    private List<BlogComment> postComments = new ArrayList<>();

}
