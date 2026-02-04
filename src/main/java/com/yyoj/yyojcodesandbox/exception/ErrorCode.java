package com.yyoj.yyojcodesandbox.exception;

public enum ErrorCode {

    SUCCESS(200, "成功"),
    ERROR(500, "系统异常"),
    PARAM_ERROR(400, "参数错误"),
    AUTH_ERROR(401, "认证失败"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(1000, "业务异常"),
    ;

    private Integer code;
    private String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
