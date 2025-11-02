package com.yyoj.yyojcodesandbox.controller;

import com.yyoj.yyojcodesandbox.JavaNativeCodeSandbox;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeRequest;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class MainController {

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    //定义请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";


    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    /**
     * 执行代码
     *
     * @param executeCodeRequest 执行代码请求
     * @return 执行结果响应对象
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest httpServletRequest,
                                           HttpServletResponse httpServletResponse) {
        String header = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(header)) {
            httpServletResponse.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空！");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
