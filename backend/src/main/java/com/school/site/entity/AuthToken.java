package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 登录令牌(落库,服务重启/发版后不掉线) */
@Data
@Entity
@Table(name = "auth_token", indexes = {
        @Index(name = "idx_authtoken_token", columnList = "token", unique = true)
})
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
