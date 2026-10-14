package com.xq.handler;

import com.xq.constant.MessageConstant;
import com.xq.exception.BaseException;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 未认证 → 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(UnauthorizedException ex) {
        log.warn("未认证：{}", ex.getMessage());
        return Result.error(ex.getMessage() == null
                ? MessageConstant.UNAUTHORIZED
                : ex.getMessage());
    }

    /**
     * 无权限 → 403
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleForbidden(ForbiddenException ex) {
        log.warn("无权限：{}", ex.getMessage());
        return Result.error(ex.getMessage() == null
                ? MessageConstant.FORBIDDEN
                : ex.getMessage());
    }

    /**
     * 业务异常（BaseException 及其它子类）
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBase(BaseException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * SQL 约束冲突（唯一键重复等）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSqlIntegrity(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String value = split.length > 2 ? split[2] : "";
            return Result.error(value + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("未知异常：", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
