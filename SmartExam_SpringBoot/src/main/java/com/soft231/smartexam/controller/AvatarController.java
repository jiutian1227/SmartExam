package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//头像管理控制器
//上传头像：上传用户头像图片到本地目录 uploadAvatar
//删除头像：移除用户头像 deleteAvatar
@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    @Value("${upload.avatar-path:./uploads/avatars}")
    private String avatarPath;

    @Autowired
    private UserService userService;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    //上传/更换头像
    @PostMapping("/upload")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        //从token中获取当前用户
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        //校验文件是否为空
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        //校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return Result.error("仅支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        //校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过 2MB");
        }

        //提取文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("无法识别的文件格式");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String extWithoutDot = ext.replace(".", "");
        if (!ALLOWED_EXTENSIONS.contains(extWithoutDot)) {
            return Result.error("不支持的文件格式");
        }

        //构建文件名：{username}.{ext}
        String fileName = user.getUsername() + ext;

        File dir = new File(avatarPath);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.error("服务器创建目录失败");
        }

        //删除旧头像
        File[] oldFiles = dir.listFiles((d, name) -> {
            String baseName = user.getUsername() + ".";
            return name.startsWith(baseName) && !name.equals(fileName);
        });
        if (oldFiles != null) {
            for (File oldFile : oldFiles) {
                oldFile.delete();
            }
        }

        //保存
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }

        //更新数据库
        String avatarUrl = "/uploads/avatars/" + fileName;
        user.setAvatar(avatarUrl);
        userService.updateById(user);

        return Result.success(avatarUrl);
    }

    //删除头像
    @DeleteMapping
    public Result<Void> deleteAvatar(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 删除物理文件
        if (user.getAvatar() != null) {
            // 从 avatar URL 反推出文件路径
            String relativePath = user.getAvatar().replace("/uploads/avatars/", "");
            File file = new File(avatarPath, relativePath);
            if (file.exists()) {
                file.delete();
            }
        }

        // 清空数据库 avatar 字段
        user.setAvatar(null);
        userService.updateById(user);

        return Result.success();
    }

    //从请求头解析用户ID
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();
        // 兼容旧格式 token_xxx
        if (token.startsWith("token_")) {
            try {
                return Long.parseLong(token.substring(6));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // JWT格式
        return JwtUtil.getUserId(token);
    }
}
