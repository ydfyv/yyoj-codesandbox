package com.yyoj.yyojcodesandbox.model;

import lombok.Data;

@Data
public class ExecuteMessage {

    // 执行码
    private Integer exitCode;

    // 执行结果信息
    private String message;

    // 执行错误信息
    private String errorMessage;

    // 执行时间
    private Long executeTime;
}
