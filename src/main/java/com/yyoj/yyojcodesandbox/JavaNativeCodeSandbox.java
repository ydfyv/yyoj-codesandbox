package com.yyoj.yyojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.yyoj.yyojcodesandbox.enums.ExecuteCodeStatusEnum;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeRequest;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeResponse;
import com.yyoj.yyojcodesandbox.model.ExecuteMessage;
import com.yyoj.yyojcodesandbox.model.JudgeInfo;
import com.yyoj.yyojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        String code = request.getCode();
        List<String> inputList = request.getInput();

        String userDir = System.getProperty("user.dir");

        String globalCodePath = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        if (FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }

        // 将用户代码隔离存放
        String userCodeParentPath = globalCodePath + File.separator + UUID.randomUUID();

        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;

        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());


        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            ExecuteMessage compileMessage = ProcessUtils.executeAndGetMessage(compileProcess, "编译");

//            ExecuteMessage compileMessage = ProcessUtils.executeInteractAndGetMessage(compileProcess, "编译");

            System.out.println(compileMessage);
        } catch (IOException e) {
            return getErrorResponse(e);
        }

        List<ExecuteMessage> runMessageList = new ArrayList<>();

        // 执行代码，得到输出结果
        for (String inputArgs : inputList) {
            String runCommand = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCommand);

                ExecuteMessage runMessage = ProcessUtils.executeAndGetMessage(runProcess, "运行");

                runMessageList.add(runMessage);
//                ExecuteMessage runMessage = ProcessUtils.executeInteractAndGetMessage(runProcess, inputArgs);

                System.out.println(runMessage);
            } catch (IOException e) {
                return getErrorResponse(e);
            }
        }


        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

        // 整理输出结果
        List<String> outputList = new ArrayList<>();

        long maxTime = 0;

        for (ExecuteMessage executeMessage : runMessageList) {

            Long executeTime = executeMessage.getExecuteTime();

            if (executeTime > maxTime) {
                maxTime = executeTime;
            }

            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                // 假如执行过程中有报错信息 3 ---> 执行过程中有报错信息
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.RunError.getStatus());
                executeCodeResponse.setMessage(errorMessage);
                break;
            }
            String message = executeMessage.getMessage();
            outputList.add(message);
        }

        if (outputList.size() == inputList.size()) {
            // 1----> 运行成功
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.RunSuccess.getStatus());
        }
        executeCodeResponse.setOutputList(outputList);

        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setMessage();
//        judgeInfo.setMemory();
        // 选用最大运行时间
        judgeInfo.setTime(maxTime);

        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 删除文件
        if (userCodeParentPath != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }

        // 异常处理，提高程序的健壮性

        return executeCodeResponse;
    }

    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(ExecuteCodeStatusEnum.CodeSandboxError.getStatus());
        JudgeInfo judgeInfo = new JudgeInfo();

        executeCodeResponse.setJudgeInfo(judgeInfo);
        executeCodeResponse.setOutputList(new ArrayList<>());

        return executeCodeResponse;
    }
}
