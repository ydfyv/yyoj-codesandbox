package com.yyoj.yyojcodesandbox.exception;

/**
 * @author 阿狸
 * @date 2026-02-04
 */
public class BusinessException extends RuntimeException{

    private final Integer code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
