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

public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    // 设置程序的超时时间
    private static final long TIMEOUT = 10000L;

    // 用户代码文件父路径
//    public String userCodeParentPath = null;


    /**
     * 1.将用户的代码保存为文件
     * @param code 用户代码
     * @return 用户代码文件
     */
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        // 判断全局代码目录是否存在
        String globalCodePath = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }

        // 将用户代码隔离存放
        String userCodeParentPath = globalCodePath + File.separator + UUID.randomUUID();

        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;

        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        return userCodeFile;
    }

    /**
     * 2.编译用户代码
     * @param file 用户代码文件
     * @return 编译结果
     */
    public ExecuteMessage compileFile(File file) {
        // 编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", file.getAbsolutePath());

        ExecuteMessage compileMessage = null;

        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            compileMessage = ProcessUtils.executeAndGetMessage(compileProcess, "编译");

//            ExecuteMessage compileMessage = ProcessUtils.executeInteractAndGetMessage(compileProcess, "编译");

            if (!compileMessage.getExitCode().equals(0)) {
                throw new RuntimeException("编译错误！");
            }

            System.out.println(compileMessage);
        } catch (IOException e) {
//            return getErrorResponse(e);
            throw new RuntimeException(e);
        }
        return compileMessage;
    }

    /**
     * 3.运行用户代码
     * @param inputList 输入用例
     * @return 运行结果
     */

    public List<ExecuteMessage> runFile(List<String> inputList, File file) {

        List<ExecuteMessage> runMessageList = new ArrayList<>();

        // 执行代码，得到输出结果
        for (String inputArgs : inputList) {
            // 手动限制Java最大堆内存
//            String runCommand = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            String userCodeParentPath = file.getParent();

            String runCommand = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            System.out.println("run file cmd: " + runCommand);

            try {
                Process runProcess = Runtime.getRuntime().exec(runCommand);
                // 守护线程
                new Thread(() -> {
                    try {
                        Thread.sleep(TIMEOUT);
                        // 超时中断执行
                        if (runProcess.isAlive()) runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                ExecuteMessage runMessage = ProcessUtils.executeInteractAndGetMessage(runProcess, inputArgs);

                runMessageList.add(runMessage);
//                ExecuteMessage runMessage = ProcessUtils.executeInteractAndGetMessage(runProcess, inputArgs);

                System.out.println(runMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return runMessageList;
    }

    /**
     * 4.整理输出结果
     * @param runMessageList 运行结果
     * @return 执行代码结果
     */
    public ExecuteCodeResponse getOutResponse(List<ExecuteMessage> runMessageList) {
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

        if (outputList.size() == runMessageList.size()) {
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

        return executeCodeResponse;
    }

    /**
     * 5.删除用户代码文件
     * @return 是否删除成功
     */
    public boolean delUserCodeFile(File file) {
        // 删除文件
        if (file != null) {
            boolean del = FileUtil.del(file);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 6. 异常处理，提高程序的健壮性
     * @param e 异常
     * @return 执行代码结果响应
     */
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

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {

        String code = request.getCode();
        List<String> inputList = request.getInput();
        String language = request.getLanguage();

        // 将用户提交的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 编译用户代码
        ExecuteMessage compileMessage = compileFile(userCodeFile);

        System.out.println("编译结果" + compileMessage);

        // 运行用户代码
        List<ExecuteMessage> runMessageList = runFile(inputList, userCodeFile);

        // 整理输出结果
        ExecuteCodeResponse executeCodeResponse = getOutResponse(runMessageList);

        //删除用户代码文件
        boolean del = delUserCodeFile(userCodeFile);

        if (del) {
            System.out.println("删除成功");
        } else {
            System.out.println("删除失败");
        }

        System.out.println("响应内容： " + executeCodeResponse);

        return executeCodeResponse;
    }


}
