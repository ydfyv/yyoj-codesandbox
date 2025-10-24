package com.yyoj.yyojcodesandbox;

import cn.hutool.core.io.FileUtil;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeRequest;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;


class JavaNativeCodeSandboxTest {

    @Test
    void test1() {
        // code获取
        String code = FileUtil.readUtf8String("D:\\learn\\oj\\yyoj-codesandbox\\src\\main\\resources\\compileCode\\Main.java");

        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setCode(code);
        executeCodeRequest.setInput(Arrays.asList("1 2", "3 4"));

        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();

        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);

        System.out.println("response: " + executeCodeResponse);
    }
}