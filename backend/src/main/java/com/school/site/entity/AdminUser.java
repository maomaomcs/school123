package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "admin_user")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    /** 角色:ADMIN(校宣,可审核发布/管理全部) / EDITOR(部门负责人,只能投稿本部门文章) */
    @Column(nullable = false, length = 16)
    private String role = "EDITOR";

    /** 显示名 / 部门(如:教务处 王老师) */
    @Column(length = 60)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled = true;
}
