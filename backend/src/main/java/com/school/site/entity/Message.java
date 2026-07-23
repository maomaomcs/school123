package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 联系我们-留言/咨询 */
@Data
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 120)
    private String contact;

    @Column(nullable = false, length = 1000)
    private String content;

    private boolean handled = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
