package com.school.site.repository;

import com.school.site.entity.VisitLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    /** 某天 PV(总访问次数) */
    long countByDay(LocalDate day);

    /** 某天 UV(去重访客数) */
    @Query("select count(distinct v.visitorHash) from VisitLog v where v.day = :day")
    long uvOfDay(@Param("day") LocalDate day);

    /** 时间范围内按天聚合:[day, pv, uv] */
    @Query("select v.day, count(v), count(distinct v.visitorHash) from VisitLog v " +
            "where v.day >= :from group by v.day order by v.day")
    List<Object[]> trend(@Param("from") LocalDate from);

    /** 来源渠道 Top:[refererDomain, pv] */
    @Query("select v.refererDomain, count(v) from VisitLog v where v.day >= :from " +
            "group by v.refererDomain order by count(v) desc")
    List<Object[]> topReferers(@Param("from") LocalDate from, Pageable pageable);

    /** 热门页面 Top:[path, pv] */
    @Query("select v.path, count(v) from VisitLog v where v.day >= :from " +
            "group by v.path order by count(v) desc")
    List<Object[]> topPages(@Param("from") LocalDate from, Pageable pageable);

    /** 设备分布:[device, pv] */
    @Query("select v.device, count(v) from VisitLog v where v.day >= :from group by v.device")
    List<Object[]> deviceSplit(@Param("from") LocalDate from);

    /** 范围内总 PV / UV */
    long countByDayGreaterThanEqual(LocalDate from);

    @Query("select count(distinct v.visitorHash) from VisitLog v where v.day >= :from")
    long uvSince(@Param("from") LocalDate from);
}
