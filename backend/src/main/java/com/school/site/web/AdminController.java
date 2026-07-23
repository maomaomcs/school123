package com.school.site.web;

import com.school.site.entity.*;
import com.school.site.repository.*;
import com.school.site.service.AuthService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final ArticleRepository articleRepo;
    private final BannerRepository bannerRepo;
    private final TeacherRepository teacherRepo;
    private final SinglePageRepository pageRepo;
    private final MessageRepository messageRepo;

    public AdminController(AuthService authService, ArticleRepository articleRepo, BannerRepository bannerRepo,
                           TeacherRepository teacherRepo, SinglePageRepository pageRepo, MessageRepository messageRepo) {
        this.authService = authService;
        this.articleRepo = articleRepo;
        this.bannerRepo = bannerRepo;
        this.teacherRepo = teacherRepo;
        this.pageRepo = pageRepo;
        this.messageRepo = messageRepo;
    }

    // ---------- 当前用户 / 权限 ----------
    private AdminUser current(HttpServletRequest req) {
        return authService.getByUsername(String.valueOf(req.getAttribute("adminUser")));
    }
    private boolean isAdmin(AdminUser u) { return AuthService.ROLE_ADMIN.equals(u.getRole()); }
    private void requireAdmin(AdminUser u) {
        if (!isAdmin(u)) throw new ApiException(403, "无权操作,仅校宣管理员可用");
    }

    // ---------- 认证 ----------
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("username"), body.get("password"));
        AdminUser u = authService.getByUsername(body.get("username"));
        return Map.of("token", token, "username", u.getUsername(),
                "role", u.getRole(), "displayName", nz(u.getDisplayName(), u.getUsername()));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest req) {
        authService.logout(tokenOf(req));
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest req) {
        AdminUser u = current(req);
        return Map.of("username", u.getUsername(), "role", u.getRole(),
                "displayName", nz(u.getDisplayName(), u.getUsername()));
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(HttpServletRequest req, @RequestBody Map<String, String> body) {
        authService.changePassword(current(req).getUsername(), body.get("oldPassword"), body.get("newPassword"));
        authService.logout(tokenOf(req));
        return Map.of("ok", true, "message", "密码已修改,请重新登录");
    }

    private String tokenOf(HttpServletRequest req) {
        String t = req.getHeader("X-Admin-Token");
        if (t == null) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) t = auth.substring(7);
        }
        return t;
    }

    // ---------- 文章 ----------
    @GetMapping("/articles")
    public Map<String, Object> articles(HttpServletRequest req,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        AdminUser me = current(req);
        final boolean editor = !isAdmin(me);
        Specification<Article> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (StringUtils.hasText(category)) ps.add(cb.equal(root.get("category"), category));
            if (StringUtils.hasText(status)) ps.add(cb.equal(root.get("status"), status));
            if (editor) ps.add(cb.equal(root.get("authorId"), me.getId())); // 编辑只看自己的
            return cb.and(ps.toArray(new Predicate[0]));
        };
        PageRequest pr = PageRequest.of(Math.max(0, page - 1), Math.min(100, size), Sort.by(Sort.Direction.DESC, "id"));
        Page<Article> p = articleRepo.findAll(spec, pr);
        List<Map<String, Object>> list = p.getContent().stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("category", a.getCategory());
            m.put("status", nz(a.getStatus(), a.isPublished() ? "published" : "draft"));
            m.put("published", a.isPublished());
            m.put("top", a.isTop());
            m.put("views", a.getViews());
            m.put("authorName", a.getAuthorName());
            m.put("rejectReason", a.getRejectReason());
            m.put("publishedAt", a.getPublishedAt());
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> res = new HashMap<>();
        res.put("list", list);
        res.put("total", p.getTotalElements());
        res.put("page", page);
        res.put("size", size);
        res.put("pages", p.getTotalPages());
        // 给校宣返回待审核数量,便于菜单红点
        if (isAdmin(me)) res.put("pendingCount", articleRepo.countByStatus("pending"));
        return res;
    }

    @GetMapping("/articles/{id}")
    public Article getArticle(HttpServletRequest req, @PathVariable Long id) {
        AdminUser me = current(req);
        Article a = articleRepo.findById(id).orElseThrow(() -> new ApiException(404, "文章不存在"));
        if (!isAdmin(me) && !me.getId().equals(a.getAuthorId())) throw new ApiException(403, "只能查看自己投稿的文章");
        return a;
    }

    @PostMapping("/articles")
    public Article createArticle(HttpServletRequest req, @RequestBody Article body,
                                 @RequestParam(defaultValue = "draft") String action) {
        AdminUser me = current(req);
        Article a = new Article();
        applyFields(a, body);
        a.setAuthorId(me.getId());
        a.setAuthorName(nz(me.getDisplayName(), me.getUsername()));
        a.setCreatedAt(LocalDateTime.now());
        if (a.getPublishedAt() == null) a.setPublishedAt(LocalDateTime.now());
        applyStatus(a, me, action);
        return articleRepo.save(a);
    }

    @PutMapping("/articles/{id}")
    public Article updateArticle(HttpServletRequest req, @PathVariable Long id,
                                 @RequestBody Article body, @RequestParam(defaultValue = "draft") String action) {
        AdminUser me = current(req);
        Article a = articleRepo.findById(id).orElseThrow(() -> new ApiException(404, "文章不存在"));
        if (!isAdmin(me) && !me.getId().equals(a.getAuthorId())) throw new ApiException(403, "只能编辑自己投稿的文章");
        applyFields(a, body);
        if (body.getPublishedAt() != null) a.setPublishedAt(body.getPublishedAt());
        applyStatus(a, me, action);
        return articleRepo.save(a);
    }

    private void applyFields(Article a, Article body) {
        a.setTitle(body.getTitle());
        a.setCategory(body.getCategory());
        a.setSummary(body.getSummary());
        a.setContent(body.getContent());
        a.setCover(body.getCover());
        a.setAuthor(body.getAuthor());
        a.setSource(body.getSource());
        a.setTop(body.isTop());
    }

    /** 依据角色 + 动作决定审核状态 */
    private void applyStatus(Article a, AdminUser me, String action) {
        if (isAdmin(me)) {
            if ("publish".equals(action)) {
                a.setStatus("published");
                a.setPublished(true);
                a.setRejectReason(null);
                if (a.getPublishedAt() == null) a.setPublishedAt(LocalDateTime.now());
            } else { // 存草稿
                a.setStatus("draft");
                a.setPublished(false);
            }
        } else { // 编辑
            if ("submit".equals(action)) {
                a.setStatus("pending");
            } else {
                a.setStatus("draft");
            }
            a.setPublished(false); // 编辑无发布权
            a.setRejectReason(null);
        }
    }

    /** 校宣审核通过 */
    @PostMapping("/articles/{id}/approve")
    public Map<String, Object> approve(HttpServletRequest req, @PathVariable Long id) {
        requireAdmin(current(req));
        Article a = articleRepo.findById(id).orElseThrow(() -> new ApiException(404, "文章不存在"));
        a.setStatus("published");
        a.setPublished(true);
        a.setRejectReason(null);
        if (a.getPublishedAt() == null) a.setPublishedAt(LocalDateTime.now());
        articleRepo.save(a);
        return Map.of("ok", true);
    }

    /** 校宣驳回 */
    @PostMapping("/articles/{id}/reject")
    public Map<String, Object> reject(HttpServletRequest req, @PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin(current(req));
        Article a = articleRepo.findById(id).orElseThrow(() -> new ApiException(404, "文章不存在"));
        a.setStatus("rejected");
        a.setPublished(false);
        a.setRejectReason(nz(body.get("reason"), "未说明理由"));
        articleRepo.save(a);
        return Map.of("ok", true);
    }

    @DeleteMapping("/articles/{id}")
    public Map<String, Object> deleteArticle(HttpServletRequest req, @PathVariable Long id) {
        AdminUser me = current(req);
        Article a = articleRepo.findById(id).orElseThrow(() -> new ApiException(404, "文章不存在"));
        if (!isAdmin(me) && !me.getId().equals(a.getAuthorId())) throw new ApiException(403, "只能删除自己投稿的文章");
        articleRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ---------- 账号管理(仅校宣) ----------
    @GetMapping("/accounts")
    public List<Map<String, Object>> accounts(HttpServletRequest req) {
        requireAdmin(current(req));
        return authService.listUsers().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("displayName", u.getDisplayName());
            m.put("role", u.getRole());
            m.put("enabled", u.isEnabled());
            return m;
        }).collect(Collectors.toList());
    }

    @PostMapping("/accounts")
    public Map<String, Object> createAccount(HttpServletRequest req, @RequestBody Map<String, String> body) {
        requireAdmin(current(req));
        authService.createUser(body.get("username"), body.get("password"), body.get("displayName"), body.get("role"));
        return Map.of("ok", true);
    }

    @PostMapping("/accounts/{id}/reset-password")
    public Map<String, Object> resetPwd(HttpServletRequest req, @PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin(current(req));
        authService.resetPassword(id, body.get("newPassword"));
        return Map.of("ok", true);
    }

    @PatchMapping("/accounts/{id}")
    public Map<String, Object> setAccountEnabled(HttpServletRequest req, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        AdminUser me = current(req);
        requireAdmin(me);
        authService.setEnabled(id, Boolean.TRUE.equals(body.get("enabled")), me.getUsername());
        return Map.of("ok", true);
    }

    @DeleteMapping("/accounts/{id}")
    public Map<String, Object> deleteAccount(HttpServletRequest req, @PathVariable Long id) {
        AdminUser me = current(req);
        requireAdmin(me);
        authService.deleteUser(id, me.getUsername());
        return Map.of("ok", true);
    }

    // ---------- 轮播图(仅校宣) ----------
    @GetMapping("/banners")
    public List<Banner> banners(HttpServletRequest req) {
        requireAdmin(current(req));
        return bannerRepo.findAllByOrderBySortAsc();
    }

    @PostMapping("/banners")
    public Banner createBanner(HttpServletRequest req, @RequestBody Banner b) {
        requireAdmin(current(req));
        b.setId(null);
        return bannerRepo.save(b);
    }

    @PutMapping("/banners/{id}")
    public Banner updateBanner(HttpServletRequest req, @PathVariable Long id, @RequestBody Banner body) {
        requireAdmin(current(req));
        Banner b = bannerRepo.findById(id).orElseThrow(() -> new ApiException(404, "轮播图不存在"));
        b.setTitle(body.getTitle());
        b.setSubtitle(body.getSubtitle());
        b.setImageUrl(body.getImageUrl());
        b.setLink(body.getLink());
        b.setSort(body.getSort());
        b.setEnabled(body.isEnabled());
        return bannerRepo.save(b);
    }

    @DeleteMapping("/banners/{id}")
    public Map<String, Object> deleteBanner(HttpServletRequest req, @PathVariable Long id) {
        requireAdmin(current(req));
        bannerRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ---------- 师资(仅校宣) ----------
    @GetMapping("/teachers")
    public List<Teacher> teachers(HttpServletRequest req) {
        requireAdmin(current(req));
        return teacherRepo.findAllByOrderBySortAsc();
    }

    @PostMapping("/teachers")
    public Teacher createTeacher(HttpServletRequest req, @RequestBody Teacher t) {
        requireAdmin(current(req));
        t.setId(null);
        return teacherRepo.save(t);
    }

    @PutMapping("/teachers/{id}")
    public Teacher updateTeacher(HttpServletRequest req, @PathVariable Long id, @RequestBody Teacher body) {
        requireAdmin(current(req));
        Teacher t = teacherRepo.findById(id).orElseThrow(() -> new ApiException(404, "教师不存在"));
        t.setName(body.getName());
        t.setTitle(body.getTitle());
        t.setSubject(body.getSubject());
        t.setPhoto(body.getPhoto());
        t.setIntro(body.getIntro());
        t.setSort(body.getSort());
        return teacherRepo.save(t);
    }

    @DeleteMapping("/teachers/{id}")
    public Map<String, Object> deleteTeacher(HttpServletRequest req, @PathVariable Long id) {
        requireAdmin(current(req));
        teacherRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ---------- 单页(仅校宣) ----------
    @GetMapping("/pages")
    public List<SinglePage> pages(HttpServletRequest req) {
        requireAdmin(current(req));
        return pageRepo.findAll();
    }

    @PutMapping("/pages/{key}")
    public SinglePage savePage(HttpServletRequest req, @PathVariable String key, @RequestBody SinglePage body) {
        requireAdmin(current(req));
        SinglePage p = pageRepo.findById(key).orElseGet(SinglePage::new);
        p.setPageKey(key);
        p.setTitle(body.getTitle());
        p.setContent(body.getContent());
        return pageRepo.save(p);
    }

    // ---------- 留言(仅校宣) ----------
    @GetMapping("/messages")
    public List<Message> messages(HttpServletRequest req) {
        requireAdmin(current(req));
        return messageRepo.findAllByOrderByCreatedAtDesc();
    }

    @PutMapping("/messages/{id}/handled")
    public Map<String, Object> handleMessage(HttpServletRequest req, @PathVariable Long id, @RequestParam(defaultValue = "true") boolean handled) {
        requireAdmin(current(req));
        Message m = messageRepo.findById(id).orElseThrow(() -> new ApiException(404, "留言不存在"));
        m.setHandled(handled);
        messageRepo.save(m);
        return Map.of("ok", true);
    }

    @DeleteMapping("/messages/{id}")
    public Map<String, Object> deleteMessage(HttpServletRequest req, @PathVariable Long id) {
        requireAdmin(current(req));
        messageRepo.deleteById(id);
        return Map.of("ok", true);
    }

    private static String nz(String v, String def) {
        return StringUtils.hasText(v) ? v : def;
    }
}
