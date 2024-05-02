package com.example.blog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter @Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "userRoles")
    private Set<BlogUser> users;

    public Role() {

    }

    public Role(String name) {
        this.name = name;
    }


}
