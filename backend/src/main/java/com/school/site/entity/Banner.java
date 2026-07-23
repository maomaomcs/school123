package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;

/** 首页轮播图 */
@Data
@Entity
@Table(name = "banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    /** 点击跳转链接,可空 */
    @Column(length = 500)
    private String link;

    private int sort = 0;

    private boolean enabled = true;
}
