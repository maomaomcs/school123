package com.school.site.repository;

import com.school.site.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByEnabledTrueOrderBySortAsc();

    List<Banner> findAllByOrderBySortAsc();
}
