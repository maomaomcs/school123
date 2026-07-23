package com.school.site.repository;

import com.school.site.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findAllByOrderBySortAsc();
}
