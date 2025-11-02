package com.yyoj.yyojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
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
import java.util.Arrays;
import java.util.List;

public class JavaNativeCodeSandboxOld implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    // 设置程序的超时时间
    private static final int TIMEOUT = 10000;

    // 程序代码的黑名单
    private static final List<String> BLACK_LIST = Arrays.asList("File", "exec");

    private static final WordTree WORD_TREE = new WordTree();

    // 安全管理类路径
    private static final String SECURITY_MANAGER_CLASS_NAME = "D:\\learn\\oj\\yyoj-codesandbox\\src\\main\\resources\\security";

    // 安全管理类名称
    private static final String SECURITY_MANAGER_NAME = "MySecurityManager";

    static {
        // 黑名单初始化
        WORD_TREE.addWords(BLACK_LIST);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
//        // 设置系统的SecurityManager
//        System.setSecurityManager(new MySecurityManager());
        String code = request.getCode();
        List<String> inputList = request.getInput();

        // 检查代码中是否包含黑名单中的关键字
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            System.out.println("检测到非法关键字: " + foundWord.getFoundWord());
            return null;
        }

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
            // 手动限制Java最大堆内存
//            String runCommand = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            String runCommand = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, SECURITY_MANAGER_CLASS_NAME, SECURITY_MANAGER_NAME, inputArgs);

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
