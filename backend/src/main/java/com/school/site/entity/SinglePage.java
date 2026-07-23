package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;

/** 单页内容:学校简介/校长寄语/校园环境/联系我们 等,按 pageKey 取 */
@Data
@Entity
@Table(name = "single_page")
public class SinglePage {
    @Id
    @Column(name = "page_key", length = 40)
    private String pageKey;

    @Column(nullable = false, length = 120)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;
}
