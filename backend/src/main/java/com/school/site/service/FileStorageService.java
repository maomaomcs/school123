package com.school.site.service;

import com.school.site.web.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/** 文件上传:图片自动压缩(最长边1600px),按日期分目录存放 */
@Service
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException("文件为空");
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        String day = LocalDate.now().toString();
        String name = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        Path dir = Paths.get(uploadDir, day);
        try {
            Files.createDirectories(dir);
            File dest = dir.resolve(name).toFile();
            boolean isImage = ext.matches("jpg|jpeg|png|bmp");
            if (isImage) {
                BufferedImage img = ImageIO.read(file.getInputStream());
                if (img != null) {
                    BufferedImage out = shrink(img, 1600);
                    // png 保留透明,其它转 jpg
                    String fmt = ext.equals("png") ? "png" : "jpg";
                    if (!name.endsWith("." + fmt)) {
                        name = name.substring(0, name.lastIndexOf('.') + 1) + fmt;
                        dest = dir.resolve(name).toFile();
                    }
                    ImageIO.write(out, fmt, dest);
                } else {
                    file.transferTo(dest);
                }
            } else {
                file.transferTo(dest);
            }
        } catch (IOException e) {
            throw new ApiException("文件保存失败: " + e.getMessage());
        }
        return "/api/files/" + day + "/" + name;
    }

    private BufferedImage shrink(BufferedImage src, int maxEdge) {
        int w = src.getWidth(), h = src.getHeight();
        int longEdge = Math.max(w, h);
        if (longEdge <= maxEdge) return toRgb(src);
        double scale = (double) maxEdge / longEdge;
        int nw = (int) (w * scale), nh = (int) (h * scale);
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, Color.WHITE, null);
        g.dispose();
        return dst;
    }

    private BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, Color.WHITE, null);
        g.dispose();
        return dst;
    }

    public Path resolve(String subPath) {
        return Paths.get(uploadDir).resolve(subPath).normalize();
    }
}
