package com.yyoj.yyojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.yyoj.yyojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessUtils {

    /**
     * 执行命令并获取执行结果
     *
     * @param process
     * @return
     */
    public static ExecuteMessage executeAndGetMessage(Process process, String actionName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            int exitValue = process.waitFor();
            executeMessage.setExitCode(exitValue);

            if (exitValue == 0) {
                // 正常退出
                System.out.println(actionName + "成功！");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder normalOutputMessage = new StringBuilder();
                String compileNormalLine;
                while ((compileNormalLine = bufferedReader.readLine()) != null) {
                    normalOutputMessage.append(compileNormalLine);
                }
                executeMessage.setMessage(normalOutputMessage.toString());

                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorMessage = new StringBuilder();
                String errorLine;
                while ((errorLine = errorBufferedReader.readLine()) != null) {
                    errorMessage.append(errorLine);
                }
                executeMessage.setErrorMessage(errorMessage.toString());
            } else {
                // 异常退出
                System.out.println(actionName + "失败！");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder normalOutputMessage = new StringBuilder();
                String compileNormalLine;
                while ((compileNormalLine = bufferedReader.readLine()) != null) {
                    normalOutputMessage.append(compileNormalLine);
                }
                executeMessage.setMessage(normalOutputMessage.toString());

                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorMessage = new StringBuilder();
                String errorLine;
                while ((errorLine = errorBufferedReader.readLine()) != null) {
                    errorMessage.append(errorLine);
                }
                executeMessage.setErrorMessage(errorMessage.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        stopWatch.stop();
        // 获取执行时间
        executeMessage.setExecuteTime(stopWatch.getTotalTimeMillis());
        return executeMessage;
    }


    /**
     * 执行命令并获取执行结果(交互式)
     *
     * @param process
     * @param args
     * @return
     */
    public static ExecuteMessage executeInteractAndGetMessage(Process process, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        OutputStream outputStream = null;
        BufferedReader bufferedReader = null;

        try {

            outputStream = process.getOutputStream();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            String[] inputAttr = args.split(" ");
            String s = StrUtil.join("\n", inputAttr) + "\n";

            outputStreamWriter.write(s);

            outputStreamWriter.flush();

            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder normalOutputMessage = new StringBuilder();
            String compileNormalLine;
            while ((compileNormalLine = bufferedReader.readLine()) != null) {
                normalOutputMessage.append(compileNormalLine);
            }
            executeMessage.setMessage(normalOutputMessage.toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                outputStream.close();
                bufferedReader.close();
                process.destroy();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return executeMessage;
    }
}
