package com.yyoj.yyojcodesandbox;

import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yyoj.yyojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final boolean FIRST_INIT = true;

    /**
     * 执行代码， 将编译好的代码文件上传至docker中，并且启动容器执行
     * @param inputList 输入用例
     * @return
     */
    @Override
    public List<ExecuteMessage> runFile(List<String> inputList, File file) {
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
        String userCodeParentPath = file.getParent();

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

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        ExecuteMessage executeMessage = new ExecuteMessage();

        List<ExecuteMessage> runMessageList = new ArrayList<>();

        final String[] message = {null};
        final String[] errorMessage = {null};

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
                        errorMessage[0] = new String(frame.getPayload());
                    } else if (StreamType.STDOUT.equals(streamType)) {
                        System.out.println("标准输出：" + new String(frame.getPayload()));
                        message[0] = new String(frame.getPayload());
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

            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);

            runMessageList.add(executeMessage);
        }
        return runMessageList;
    }

}
