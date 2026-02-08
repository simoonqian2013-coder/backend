package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PetImageController {
    private static final long MAX_SIZE = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads/}")
    private String uploadUrlPrefix;

    @PreAuthorize("hasAuthority('pet:upload')")
    @PostMapping("/api/pets/images")
    public ApiResponse<Map<String, String>> uploadPetImage(@RequestParam("file") MultipartFile file,
                                                           HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            return ApiResponse.error(400, "图片大小不能超过 2MB");
        }
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ApiResponse.error(400, "图片格式不支持");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDir = Paths.get(uploadDir, "pets").toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename);
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            return ApiResponse.error(500, "图片保存失败");
        }
        String url = buildPublicUrl(request, normalizeUrlPrefix(uploadUrlPrefix) + "pets/" + filename);
        return ApiResponse.ok(Map.of("url", url));
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeUrlPrefix(String prefix) {
        String normalized = prefix == null ? "/uploads/" : prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private String buildPublicUrl(HttpServletRequest request, String path) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        String base = defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
        return base + path;
    }
}
