package com.yyoj.yyojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yyoj.yyojcodesandbox.enums.ExecuteCodeStatusEnum;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeRequest;
import com.yyoj.yyojcodesandbox.model.ExecuteCodeResponse;
import com.yyoj.yyojcodesandbox.model.ExecuteMessage;
import com.yyoj.yyojcodesandbox.model.JudgeInfo;
import com.yyoj.yyojcodesandbox.utils.ProcessUtils;
import jdk.nashorn.internal.ir.CallNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaDockerCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    // 设置程序的超时时间
    private static final int TIMEOUT = 10000;

    private static final boolean FIRST_INIT = true;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
//        // 设置系统的SecurityManager
//        System.setSecurityManager(new MySecurityManager());
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

        // 获取默认的docker
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        String image = "openjdk:8-jdk-alpine";

        if (FIRST_INIT) {
            // 拉取镜像


            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);

            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像：" + item.getStatus());
                    super.onNext(item);
                }
            };

            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("下载镜像失败！");
                throw new RuntimeException(e);
            }

            System.out.println("镜像下载完成！");
        }

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);

        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app"))); // 上传本地字节码文件到容器
        hostConfig.withMemory(100 * 1024 * 1024L); // 限制100M运行内存
        hostConfig.withCpuCount(1L); // 设置CPU核心数

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStdin(true)  // 标准输入
                .withAttachStdout(true)  // 标准输出
                .withAttachStderr(true) // 标准错误
                .withTty(true) // 交互终端
                .exec();

        System.out.println(createContainerResponse);

        String containerId = createContainerResponse.getId();

//        dockerClient.startContainerCmd(containerId).exec();
        // 启动容器
        for (String inputArgs : inputList) {
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .withCmd(cmdArray)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);

            String execId = execCreateCmdResponse.getId();

            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        System.out.println("错误输出：" + new String(frame.getPayload()));
                    } else if (StreamType.STDOUT.equals(streamType)) {
                        System.out.println("标准输出：" + new String(frame.getPayload()));
                    }
                    super.onNext(frame);
                }
            };

            try {
                dockerClient.execStartCmd(execId).exec(execStartResultCallback).awaitCompletion();
                System.out.println("程序执行异常");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }


        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

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

    // docker 容器内执行代码 docker exec sharp_wescoff java -cp /app Main 1 3
    public static void main(String[] args) {
        JavaDockerCodeSandbox javaDockerCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInput(Arrays.asList("1 2 ", "2 3"));

        String code = ResourceUtil.readStr("compileCode/Main.java", StandardCharsets.UTF_8);

        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }
}
