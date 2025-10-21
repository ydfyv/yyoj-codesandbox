package com.yyoj.yyojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    // 编程语言
    private String language;
    // 代码
    private String code;
    // 输入用例
    private List<String> input;
}
