package com.yyoj.yyojcodesandbox.exception;

import com.yyoj.yyojcodesandbox.common.BaseResponse;
import com.yyoj.yyojcodesandbox.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 阿狸
 * @date 2026-02-04
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleException(BusinessException e) {
        log.error("业务异常", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultUtils.error(ErrorCode.ERROR, "系统异常");
    }
}
