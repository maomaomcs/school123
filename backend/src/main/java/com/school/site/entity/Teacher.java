package com.school.site.entity;

import jakarta.persistence.*;
import lombok.Data;

/** 师资队伍 */
@Data
@Entity
@Table(name = "teacher")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    /** 职称/荣誉,如 特级教师、高级教师 */
    @Column(length = 120)
    private String title;

    /** 任教学科 */
    @Column(length = 80)
    private String subject;

    @Column(length = 500)
    private String photo;

    @Column(length = 2000)
    private String intro;

    private int sort = 0;
}
