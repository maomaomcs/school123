package com.school.site.web;

import com.school.site.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    /** 上传(受 /api/admin 拦截器保护,需登录) */
    @PostMapping("/admin/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        String url = storage.store(file);
        return Map.of("url", url);
    }

    /** 读取上传文件(公开) */
    @GetMapping("/files/**")
    public ResponseEntity<Resource> serve(jakarta.servlet.http.HttpServletRequest request) {
        String full = request.getRequestURI(); // /api/files/2026-07-22/xxx.jpg
        String sub = full.substring("/api/files/".length());
        Path base = storage.resolve("");
        Path path = storage.resolve(sub);
        // 防目录穿越:解析后必须仍在上传目录内
        if (!path.startsWith(base) || !Files.exists(path) || !Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource res = new FileSystemResource(path);
        MediaType mediaType = MediaTypeFactory.getMediaType(res).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                .body(res);
    }
}
