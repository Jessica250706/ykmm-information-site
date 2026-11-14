package com.xq.handler;

import com.xq.constant.MessageConstant;
import com.xq.exception.*;
import com.xq.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;

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
     * 答题未通过 → 403 + 业务 code 423
     * <p>
     * 前端 axios 拦截器读到 code=423 时跳转答题页。
     * HTTP 状态码用 403：语义上是"有身份但没资格"，符合 RESTful 习惯；
     * 和普通 ForbiddenException 区分靠 body 里的 code。
     */
    @ExceptionHandler(QuizRequiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleQuizRequired(QuizRequiredException e) {
        log.warn("答题未通过：{}", e.getMessage());
        return Result.error(QuizRequiredException.CODE, e.getMessage());
    }

    /**
     * 业务异常（BaseException 及其它子类）
     * <p>message 为 null 时给默认提示，避免前端弹出空消息
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBase(BaseException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage() == null
                ? MessageConstant.UNKNOWN_ERROR
                : ex.getMessage());
    }

    /**
     * 系统内部异常：完整堆栈落日志，前端返回中性提示
     */
    @ExceptionHandler(SystemException.class)
    public Result<Void> handleSystem(SystemException ex) {
        log.error("系统异常：", ex);
        return Result.error(MessageConstant.SYSTEM_ERROR);  // 如 "系统繁忙，请稍后重试"
    }

    /**
     * @RequestBody + @Validated 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = firstFieldError(ex.getBindingResult().getFieldError());
        log.warn("参数校验失败：{}", msg);
        return Result.error(msg);
    }

    /**
     * 表单/查询参数绑定校验失败（非 @RequestBody）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException ex) {
        String msg = firstFieldError(ex.getBindingResult().getFieldError());
        log.warn("参数绑定失败：{}", msg);
        return Result.error(msg);
    }

    /**
     * 方法级参数约束（@Validated 在类上，@NotNull 等直接标在 query/path 参数上）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数不合法");
        log.warn("参数约束失败：{}", msg);
        return Result.error(msg);
    }

    /**
     * MyBatis / Spring 包装后的唯一键冲突（比原始 JDBC 异常更常见）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("唯一键冲突：{}", ex.getMessage());
        return Result.error(MessageConstant.ALREADY_EXISTS);
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

    /* ==================== 工具 ==================== */

    private String firstFieldError(FieldError fieldError) {
        if (fieldError == null || fieldError.getDefaultMessage() == null) {
            return "参数校验失败";
        }
        return fieldError.getDefaultMessage();
    }
}
