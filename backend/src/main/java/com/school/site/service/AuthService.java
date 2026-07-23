package com.school.site.service;

import com.school.site.entity.AdminUser;
import com.school.site.entity.AuthToken;
import com.school.site.repository.AdminUserRepository;
import com.school.site.repository.AuthTokenRepository;
import com.school.site.web.ApiException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * 管理员/编辑登录 + 令牌校验 + 账号管理。令牌落库(auth_token 表),服务重启/发版后不掉线。
 * 角色:ADMIN(校宣) / EDITOR(部门投稿)。
 */
@Service
public class AuthService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EDITOR = "EDITOR";
    /** 令牌有效期(天) */
    private static final int TOKEN_TTL_DAYS = 30;

    private final AdminUserRepository adminRepo;
    private final AuthTokenRepository tokenRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    @Value("${app.init-admin-username}")
    private String initUser;
    @Value("${app.init-admin-password}")
    private String initPass;

    public AuthService(AdminUserRepository adminRepo, AuthTokenRepository tokenRepo) {
        this.adminRepo = adminRepo;
        this.tokenRepo = tokenRepo;
    }

    @PostConstruct
    public void initAdmin() {
        AdminUser u = adminRepo.findByUsername(initUser).orElseGet(AdminUser::new);
        boolean isNew = u.getId() == null;
        if (isNew) {
            u.setUsername(initUser);
            u.setPasswordHash(encoder.encode(initPass));
            u.setDisplayName("校宣");
        }
        // 确保初始账号是管理员且启用(兼容老库:role 为空时补 ADMIN)
        if (!StringUtils.hasText(u.getRole())) u.setRole(ROLE_ADMIN);
        u.setEnabled(true);
        if (isNew) u.setRole(ROLE_ADMIN);
        adminRepo.save(u);
    }

    @Transactional
    public String login(String username, String password) {
        AdminUser u = adminRepo.findByUsername(username)
                .orElseThrow(() -> new ApiException(401, "用户名或密码错误"));
        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new ApiException(401, "用户名或密码错误");
        }
        if (!u.isEnabled()) throw new ApiException(403, "账号已被停用,请联系校宣管理员");
        // 顺手清理过期令牌
        try { tokenRepo.deleteByExpiresAtBefore(LocalDateTime.now()); } catch (Exception ignored) {}
        byte[] buf = new byte[24];
        random.nextBytes(buf);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
        AuthToken t = new AuthToken();
        t.setToken(token);
        t.setUsername(username);
        t.setCreatedAt(LocalDateTime.now());
        t.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_TTL_DAYS));
        tokenRepo.save(t);
        return token;
    }

    public String usernameOf(String token) {
        if (token == null) return null;
        return tokenRepo.findByToken(token)
                .filter(t -> t.getExpiresAt() == null || t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(AuthToken::getUsername)
                .orElse(null);
    }

    public boolean valid(String token) {
        String u = usernameOf(token);
        return u != null && adminRepo.findByUsername(u).map(AdminUser::isEnabled).orElse(false);
    }

    @Transactional
    public void logout(String token) {
        if (token != null) tokenRepo.deleteByToken(token);
    }

    public AdminUser getByUsername(String username) {
        return adminRepo.findByUsername(username)
                .orElseThrow(() -> new ApiException(401, "账号不存在"));
    }

    public boolean isAdmin(String username) {
        return adminRepo.findByUsername(username).map(x -> ROLE_ADMIN.equals(x.getRole())).orElse(false);
    }

    public void changePassword(String username, String oldPwd, String newPwd) {
        AdminUser u = getByUsername(username);
        if (!encoder.matches(oldPwd, u.getPasswordHash())) {
            throw new ApiException(400, "原密码不正确");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new ApiException(400, "新密码至少6位");
        }
        u.setPasswordHash(encoder.encode(newPwd));
        adminRepo.save(u);
    }

    // ---------- 账号管理(仅 ADMIN 调用,权限在 Controller 校验) ----------

    public List<AdminUser> listUsers() {
        return adminRepo.findAllByOrderByIdAsc();
    }

    public AdminUser createUser(String username, String password, String displayName, String role) {
        username = username == null ? "" : username.trim();
        if (username.length() < 3) throw new ApiException(400, "用户名至少3位");
        if (adminRepo.existsByUsername(username)) throw new ApiException(400, "用户名已存在");
        if (password == null || password.length() < 6) throw new ApiException(400, "密码至少6位");
        AdminUser u = new AdminUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setDisplayName(StringUtils.hasText(displayName) ? displayName.trim() : username);
        u.setRole(ROLE_ADMIN.equals(role) ? ROLE_ADMIN : ROLE_EDITOR);
        u.setEnabled(true);
        return adminRepo.save(u);
    }

    public void resetPassword(Long id, String newPwd) {
        if (newPwd == null || newPwd.length() < 6) throw new ApiException(400, "新密码至少6位");
        AdminUser u = adminRepo.findById(id).orElseThrow(() -> new ApiException(404, "账号不存在"));
        u.setPasswordHash(encoder.encode(newPwd));
        adminRepo.save(u);
    }

    public void setEnabled(Long id, boolean enabled, String currentUsername) {
        AdminUser u = adminRepo.findById(id).orElseThrow(() -> new ApiException(404, "账号不存在"));
        if (u.getUsername().equals(currentUsername) && !enabled) throw new ApiException(400, "不能停用当前登录的账号");
        if (!enabled && ROLE_ADMIN.equals(u.getRole()) && adminRepo.countByRole(ROLE_ADMIN) <= 1)
            throw new ApiException(400, "至少保留一个校宣管理员");
        u.setEnabled(enabled);
        adminRepo.save(u);
    }

    public void deleteUser(Long id, String currentUsername) {
        AdminUser u = adminRepo.findById(id).orElseThrow(() -> new ApiException(404, "账号不存在"));
        if (u.getUsername().equals(currentUsername)) throw new ApiException(400, "不能删除当前登录的账号");
        if (ROLE_ADMIN.equals(u.getRole()) && adminRepo.countByRole(ROLE_ADMIN) <= 1)
            throw new ApiException(400, "至少保留一个校宣管理员");
        adminRepo.delete(u);
    }
}
