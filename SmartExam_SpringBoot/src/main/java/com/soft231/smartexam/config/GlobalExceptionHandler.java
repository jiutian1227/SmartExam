package com.soft231.smartexam.config;

import com.soft231.smartexam.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的所有异常，返回统一格式的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务参数/状态不合法（如未开考就交卷、超时提交、重复提交）：返回400 + 明确原因
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        return Result.error(400, message == null || message.isBlank() ? "请求参数不合法" : message);
    }

    /**
     * 处理运行时异常
     * @param e 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("业务运行时异常", e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理越权异常：Controller按JWT身份做归属校验失败时抛出，返回403
     * 与SecurityConfig的accessDeniedHandler保持一致的响应格式，便于前端统一提示
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        String message = e.getMessage();
        return Result.error(403, message == null || message.isBlank() ? "无权限执行此操作" : message);
    }

    /**
     * 处理所有其他异常
     * 详细堆栈只写日志，不回传前端，避免泄漏 SQL、文件路径等内部实现细节
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理的系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
