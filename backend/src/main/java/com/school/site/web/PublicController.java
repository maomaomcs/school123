package com.school.site.web;

import com.school.site.entity.*;
import com.school.site.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PublicController {

    /** 栏目定义:key -> 中文名。前端导航与列表页共用 */
    public static final LinkedHashMap<String, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("xwzx", "校园新闻");
        CATEGORIES.put("tzgg", "通知公告");
        CATEGORIES.put("jyjx", "教育教学");
        CATEGORIES.put("dycd", "德育天地");
        CATEGORIES.put("xyfc", "校园风采");
        CATEGORIES.put("zsks", "招生招考");
    }

    private final ArticleRepository articleRepo;
    private final BannerRepository bannerRepo;
    private final TeacherRepository teacherRepo;
    private final SinglePageRepository pageRepo;
    private final MessageRepository messageRepo;

    @Value("${app.site-name}")
    private String siteName;
    @Value("${app.site-en}")
    private String siteEn;
    @Value("${app.beian}")
    private String beian;

    public PublicController(ArticleRepository articleRepo, BannerRepository bannerRepo,
                            TeacherRepository teacherRepo, SinglePageRepository pageRepo,
                            MessageRepository messageRepo) {
        this.articleRepo = articleRepo;
        this.bannerRepo = bannerRepo;
        this.teacherRepo = teacherRepo;
        this.pageRepo = pageRepo;
        this.messageRepo = messageRepo;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        List<Map<String, String>> cats = CATEGORIES.entrySet().stream()
                .map(e -> Map.of("key", e.getKey(), "label", e.getValue()))
                .collect(Collectors.toList());
        Map<String, Object> m = new HashMap<>();
        m.put("siteName", siteName);
        m.put("siteEn", siteEn);
        m.put("beian", beian);
        m.put("categories", cats);
        return m;
    }

    @GetMapping("/banners")
    public List<Banner> banners() {
        return bannerRepo.findByEnabledTrueOrderBySortAsc();
    }

    /** 首页聚合:轮播 + 各栏目最新 + 师资预览,减少请求数 */
    @GetMapping("/home")
    public Map<String, Object> home() {
        Map<String, Object> m = new HashMap<>();
        m.put("banners", bannerRepo.findByEnabledTrueOrderBySortAsc());
        Map<String, List<Map<String, Object>>> sections = new LinkedHashMap<>();
        for (String key : List.of("xwzx", "tzgg", "jyjx", "dycd")) {
            sections.put(key, articleRepo
                    .findTop6ByCategoryAndPublishedTrueOrderByTopDescPublishedAtDesc(key)
                    .stream().map(this::card).collect(Collectors.toList()));
        }
        m.put("sections", sections);
        m.put("teachers", teacherRepo.findAllByOrderBySortAsc().stream().limit(6).collect(Collectors.toList()));
        List<Map<String, Object>> gallery = articleRepo
                .findTop6ByCategoryAndPublishedTrueOrderByTopDescPublishedAtDesc("xyfc")
                .stream().map(this::card).collect(Collectors.toList());
        m.put("gallery", gallery);
        return m;
    }

    @GetMapping("/articles")
    public Map<String, Object> articles(@RequestParam(required = false) String category,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        PageRequest pr = PageRequest.of(Math.max(0, page - 1), Math.min(50, size));
        Page<Article> p = (category == null || category.isBlank())
                ? articleRepo.findByPublishedTrueOrderByPublishedAtDesc(pr)
                : articleRepo.findByCategoryAndPublishedTrueOrderByTopDescPublishedAtDesc(category, pr);
        Map<String, Object> m = new HashMap<>();
        m.put("list", p.getContent().stream().map(this::card).collect(Collectors.toList()));
        m.put("total", p.getTotalElements());
        m.put("page", page);
        m.put("size", size);
        m.put("pages", p.getTotalPages());
        m.put("categoryLabel", category == null ? "全部" : CATEGORIES.getOrDefault(category, category));
        return m;
    }

    @GetMapping("/articles/{id}")
    @Transactional
    public Map<String, Object> article(@PathVariable Long id) {
        Article a = articleRepo.findById(id)
                .filter(Article::isPublished)
                .orElseThrow(() -> new ApiException(404, "文章不存在或已下线"));
        a.setViews(a.getViews() + 1);
        articleRepo.save(a);
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("category", a.getCategory());
        m.put("categoryLabel", CATEGORIES.getOrDefault(a.getCategory(), a.getCategory()));
        m.put("content", a.getContent());
        m.put("author", a.getAuthor());
        m.put("source", a.getSource());
        m.put("views", a.getViews());
        m.put("cover", a.getCover());
        m.put("publishedAt", a.getPublishedAt());
        // 上一篇 / 下一篇(同栏目)
        Article prev = articleRepo.findFirstByCategoryAndPublishedTrueAndPublishedAtLessThanOrderByPublishedAtDesc(a.getCategory(), a.getPublishedAt());
        Article next = articleRepo.findFirstByCategoryAndPublishedTrueAndPublishedAtGreaterThanOrderByPublishedAtAsc(a.getCategory(), a.getPublishedAt());
        m.put("prev", prev == null ? null : Map.of("id", prev.getId(), "title", prev.getTitle()));
        m.put("next", next == null ? null : Map.of("id", next.getId(), "title", next.getTitle()));
        return m;
    }

    /** 站内搜索 */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(name = "q", required = false) String q,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> m = new HashMap<>();
        String kw = q == null ? "" : q.trim();
        if (kw.isEmpty()) {
            m.put("list", List.of());
            m.put("total", 0L);
            m.put("page", page);
            m.put("size", size);
            m.put("pages", 0);
            m.put("keyword", "");
            return m;
        }
        PageRequest pr = PageRequest.of(Math.max(0, page - 1), Math.min(50, size));
        Page<Article> p = articleRepo.search(kw, pr);
        m.put("list", p.getContent().stream().map(this::card).collect(Collectors.toList()));
        m.put("total", p.getTotalElements());
        m.put("page", page);
        m.put("size", size);
        m.put("pages", p.getTotalPages());
        m.put("keyword", kw);
        return m;
    }

    @GetMapping("/teachers")
    public List<Teacher> teachers() {
        return teacherRepo.findAllByOrderBySortAsc();
    }

    @GetMapping("/pages/{key}")
    public SinglePage page(@PathVariable String key) {
        return pageRepo.findById(key)
                .orElseThrow(() -> new ApiException(404, "页面不存在"));
    }

    @PostMapping("/messages")
    public Map<String, Object> submitMessage(@Valid @RequestBody MessageForm form) {
        Message msg = new Message();
        msg.setName(form.getName());
        msg.setContact(form.getContact());
        msg.setContent(form.getContent());
        messageRepo.save(msg);
        return Map.of("ok", true, "message", "留言已提交,感谢您的关注!");
    }

    private Map<String, Object> card(Article a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("category", a.getCategory());
        m.put("categoryLabel", CATEGORIES.getOrDefault(a.getCategory(), a.getCategory()));
        m.put("summary", a.getSummary());
        m.put("cover", a.getCover());
        m.put("views", a.getViews());
        m.put("top", a.isTop());
        m.put("publishedAt", a.getPublishedAt());
        return m;
    }

    @Data
    public static class MessageForm {
        @NotBlank(message = "请填写称呼")
        private String name;
        private String contact;
        @NotBlank(message = "请填写留言内容")
        private String content;
    }
}
