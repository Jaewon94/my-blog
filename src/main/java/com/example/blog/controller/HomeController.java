package com.example.blog.controller;

import com.example.blog.domain.entity.BlogPost;
import com.example.blog.domain.entity.BlogUser;
import com.example.blog.service.BlogPostService;
import com.example.blog.service.BlogUserService;
import com.example.blog.util.UserProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BlogUserService blogUserService;
    private final BlogPostService blogPostService;

    @GetMapping({"/","/home"})
    public String home(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            // 사용자 정보 조회 로직
            BlogUser user = blogUserService.findByUsername(username);
            model.addAttribute("user", user);
        }

        List<BlogPost> latest9Posts = blogPostService.findLatest9Posts();
        model.addAttribute("latest9Posts", latest9Posts);

        // 댓글이 많은 게시글 상위 5개 조회
        List<BlogPost> topPostsByComments = blogPostService.findTopPostsByComments(); // 가정한 메소드
        model.addAttribute("topPostsByComments", topPostsByComments);

        return "home";
    }


}
