package com.example.workorder.entity;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.example.workorder.config.OssProperties;
import com.example.workorder.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OssService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final OssProperties ossProperties;

    /**
     * 通用头像上传入口，默认保存到 uploads 目录。
     */
    public String upload(MultipartFile file) {
        return upload(file, "uploads");
    }

    /**
     * 工单图片上传入口。
     * 工单图片和头像分目录存储，便于后续权限校验、清理和分类管理。
     */
    public String uploadWorkOrderImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传图片不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("单张图片不能超过5MB");
        }
        if (!IMAGE_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("仅支持 JPG、PNG、GIF、WEBP 图片");
        }
        return upload(file, "work-orders");
    }

    public boolean isWorkOrderImageUrl(String url) {
        return url != null && url.startsWith(normalizedUrlPrefix() + "work-orders/");
    }

    /**
     * OSS 实际上传方法。
     * 数据库只保存文件 URL，不保存文件二进制内容，减轻数据库压力。
     */
    private String upload(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = directory + "/"
                + LocalDate.now()
                + "/"
                + UUID.randomUUID()
                + suffix;

        // OSSClient 是轻量客户端，这里按次创建并在 finally 中关闭，避免连接资源泄露。
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );

        try {
            ossClient.putObject(
                    ossProperties.getBucketName(),
                    objectName,
                    file.getInputStream()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }

        return normalizedUrlPrefix() + objectName;
    }

    private String normalizedUrlPrefix() {
        // 统一保证 URL 前缀以 / 结尾，避免拼接后的图片地址缺少斜杠。
        String prefix = ossProperties.getUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }
}
