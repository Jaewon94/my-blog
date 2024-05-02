package com.example.blog.repository;

import com.example.blog.domain.entity.BlogUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BlogUserRepository extends JpaRepository<BlogUser, Long> {
    BlogUser findByVerificationCode(String code);
    Optional<BlogUser> findByUsername(String username);

    // 월별 사용자 수 그래프를 위한 코드
    @Query("SELECT new map(MONTH(u.createdDate) AS month, COUNT(u) AS count) FROM BlogUser u WHERE YEAR(u.createdDate) = :year GROUP BY MONTH(u.createdDate)")
    List<Map<String, Object>> countNewUsersByMonth(@Param("year") int year);


    BlogUser findUsernameByUserEmail(String email);

    Optional<BlogUser> findByUserEmail(String email);


    boolean existsByUserEmail(String email);

}
