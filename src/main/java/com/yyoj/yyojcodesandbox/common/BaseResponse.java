package com.yyoj.yyojcodesandbox.common;

import com.yyoj.yyojcodesandbox.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 阿狸
 * @date 2026-02-04
 */
@Data
public class  BaseResponse<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public BaseResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = null;
    }

    public BaseResponse(T data) {
        this.code = 200;
        this.message = "";
        this.data = data;
    }
}
