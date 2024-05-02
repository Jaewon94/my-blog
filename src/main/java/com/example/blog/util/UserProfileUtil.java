package com.example.blog.util;

import com.example.blog.domain.entity.BlogUser;
import lombok.Getter;

public class UserProfileUtil {


    // 프로필 사진 경로 반환 메소드를 유틸리티 메소드로 정의
    public static String getProfilePicturePath(BlogUser user) {
        if (user == null || user.getProfilePicturePath() == null || user.getProfilePicturePath().isEmpty()) {
            return "/static/images/default-profile.png"; // 기본 이미지 경로 반환
        } else {
            return user.getProfilePicturePath();
        }
    }
//    public String profilePicturePath(BlogUser user) {
//        if(user == null) {
//            return "none";
//        }
//        else {
//            return user.getProfilePicturePath();
//        }
//    }
}
