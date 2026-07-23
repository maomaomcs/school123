package com.school.site.repository;

import com.school.site.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    long countByStatus(String status);

    Page<Article> findByCategoryAndPublishedTrueOrderByTopDescPublishedAtDesc(String category, Pageable pageable);

    Page<Article> findByPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    List<Article> findTop6ByCategoryAndPublishedTrueOrderByTopDescPublishedAtDesc(String category);

    /** 站内搜索:标题/摘要/正文 模糊匹配(仅已发布) */
    @Query("select a from Article a where a.published = true and " +
            "(a.title like %:kw% or a.summary like %:kw% or a.content like %:kw%) " +
            "order by a.publishedAt desc")
    Page<Article> search(@Param("kw") String kw, Pageable pageable);

    /** 上一篇:同栏目、发布时间更早的最近一篇 */
    Article findFirstByCategoryAndPublishedTrueAndPublishedAtLessThanOrderByPublishedAtDesc(String category, LocalDateTime publishedAt);

    /** 下一篇:同栏目、发布时间更晚的最近一篇 */
    Article findFirstByCategoryAndPublishedTrueAndPublishedAtGreaterThanOrderByPublishedAtAsc(String category, LocalDateTime publishedAt);
}
