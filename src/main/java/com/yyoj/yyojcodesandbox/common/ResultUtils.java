package com.yyoj.yyojcodesandbox.common;

import com.yyoj.yyojcodesandbox.exception.ErrorCode;

/**
 * @author 阿狸
 * @date 2026-02-04
 */
public class ResultUtils {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(data);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<T>(errorCode);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String msg) {
        return new BaseResponse<T>(errorCode.getCode(), msg);
    }

    public static <T> BaseResponse<T> error(Integer code, String msg) {
        return new BaseResponse<T>(code, msg);
    }

    public static <T> BaseResponse<T> error(String msg) {
        return new BaseResponse<T>(500, msg);
    }
}
