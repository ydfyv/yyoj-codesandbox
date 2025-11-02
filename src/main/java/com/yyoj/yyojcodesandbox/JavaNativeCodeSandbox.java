package com.yyoj.yyojcodesandbox;

import com.yyoj.yyojcodesandbox.model.ExecuteCodeRequest;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * Java原生代码沙箱实现
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        System.out.println("加强打印日志");
        return super.executeCode(request);
    }
}
