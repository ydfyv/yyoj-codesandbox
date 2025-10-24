package com.yyoj.yyojcodesandbox.enums;

import java.util.Objects;

public enum ExecuteCodeStatusEnum {
    RunSuccess(1, "运行成功"),
    CodeSandboxError(2, "代码沙箱错误"),
    RunError(3, "运行过程中有报错信息"),
    ;

    private final Integer status;
    private final String description;

    ExecuteCodeStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static ExecuteCodeStatusEnum getByCode(Integer status) {
        for (ExecuteCodeStatusEnum value : ExecuteCodeStatusEnum.values()) {
            if (Objects.equals(value.getStatus(), status)) {
                return value;
            }
        }
        return null;
    }
}
