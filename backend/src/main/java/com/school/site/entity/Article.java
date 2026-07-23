package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 文章:新闻/通知/教学/德育/校园风采/招生 等,通过 category 区分 */
@Data
@Entity
@Table(name = "article", indexes = {
        @Index(name = "idx_article_cat", columnList = "category"),
        @Index(name = "idx_article_pub", columnList = "published")
})
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /** 栏目 key:xwzx/tzgg/jyjx/dycd/xyfc/zsks */
    @Column(nullable = false, length = 40)
    private String category;

    @Column(length = 500)
    private String summary;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /** 封面图 url */
    @Column(length = 500)
    private String cover;

    @Column(length = 80)
    private String author;

    @Column(length = 120)
    private String source;

    private long views = 0;

    /** 置顶 */
    private boolean top = false;

    /** 是否发布(对外可见);由审核状态驱动:status=published 时为 true */
    private boolean published = true;

    /** 审核状态:draft 草稿 / pending 待审核 / published 已发布 / rejected 已驳回 */
    @Column(length = 16)
    private String status = "published";

    /** 驳回理由(校宣填写) */
    @Column(length = 300)
    private String rejectReason;

    /** 投稿人(admin_user.id) */
    private Long authorId;

    /** 投稿人显示名(冗余,便于列表展示) */
    @Column(length = 60)
    private String authorName;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime publishedAt = LocalDateTime.now();
}
