package com.example.blog.controller;

import com.example.blog.domain.entity.BlogPost;
import com.example.blog.domain.entity.BlogUser;
import com.example.blog.service.AdminService;
import com.example.blog.service.BlogPostService;
import com.example.blog.service.BlogUserService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final BlogUserService blogUserService;
    private final BlogPostService blogPostService;


    @GetMapping("/dashboard")
    public String adminDashboard(Model model) throws JsonProcessingException {
        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("totalPosts", adminService.getTotalPosts());
        model.addAttribute("totalComments", adminService.getTotalComments());

        int currentYear = Year.now().getValue();
        // 월별 사용자 생성 수 가져오기
        List<Map<String, Object>> newUserStats = blogUserService.countNewUsersByMonth(currentYear);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(newUserStats);
        json = json.replace("\"", ""); // 따옴표 제거
        model.addAttribute("newUserStats", json);
//        model.addAttribute("newUserStats", newUserStats);

        // 월별 게시물 생성 수 가져오기
        List<Map<String, Object>> newPostStats = blogPostService.countNewPostsByMonth(currentYear);
        ObjectMapper mapper2 = new ObjectMapper();
        String json2 = mapper2.writeValueAsString(newUserStats);
        json2 = json.replace("\"", ""); // 따옴표 제거

        model.addAttribute("newPostStats", json2);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        // 사용자 관리 로직 구현
        model.addAttribute("users", blogUserService.findAllNonAdminUsers());
        return "admin/users"; // 사용자 관리 페이지로 이동
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUsers(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        blogUserService.getDeleteUser(id);
        redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 삭제되었습니다.");
        return "redirect:/admin/users";
    }

    @GetMapping("/posts")
    public String managePosts(Model model,
                              @PageableDefault(size = 15, sort = "postCreatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BlogPost> posts = blogPostService.getAllPosts(pageable);
        model.addAttribute("page", posts);
        return "admin/posts"; // 관리자 포스트 관리 페이지로 이동
    }

    @GetMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable("id") Long postId, RedirectAttributes redirectAttributes) {
        blogPostService.deletePostById(postId);
        redirectAttributes.addFlashAttribute("successMessage", "포스트가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/posts";
    }

}
