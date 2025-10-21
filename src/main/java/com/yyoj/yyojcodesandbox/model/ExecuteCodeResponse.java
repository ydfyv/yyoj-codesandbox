package com.yyoj.yyojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeResponse {

    // 执行信息
    private String message;

    // 执行状态
    private Integer status;

    // 判题信息
    private JudgeInfo judgeInfo;

    // 执行结果
    private List<String> outputList;
}
