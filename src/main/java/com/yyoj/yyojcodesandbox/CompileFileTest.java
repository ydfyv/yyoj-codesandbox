package com.yyoj.yyojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CompileFileTest {

    public static void main(String[] args) {
        String property = System.getProperty("user.dir");

        String globalCodePathName = property + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "test";

        String code = FileUtil.readUtf8String("D:\\learn\\oj\\yyoj-codesandbox\\src\\main\\resources\\compileCode\\Main.java");

        if (FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 将用户的代码隔离存饭
        String userCodePathName = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodePathName + File.separator + "Main.java";

        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        String compileCommand = String.format("javac -encoding utf-8 %s ", userCodeFile.getAbsolutePath());

        StringBuilder compileNormalSb = new StringBuilder();

        StringBuilder errorSb = new StringBuilder();

        BufferedReader bufferedReader = null;

        BufferedReader errorReader = null;

        try {
            Process process = Runtime.getRuntime().exec(compileCommand);

            process.waitFor();

            InputStream inputStream = process.getInputStream();

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String normalLine;

            while ((normalLine = bufferedReader.readLine()) != null) {
                compileNormalSb.append(normalLine).append("\n");
            }

            InputStream errorStream = process.getErrorStream();

            errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));

            String errorLine;

            while ((errorLine = errorReader.readLine()) != null) {
                errorSb.append(errorLine).append("\n");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println(errorSb);
            throw new RuntimeException(e);
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (errorReader != null) errorReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String runCommand = String.format("java -Dfile.encoding=UTF-8 -cp  %s Main 1 2", userCodePathName);

        bufferedReader = null;
        errorReader = null;

        StringBuilder runCompileNormalSb = new StringBuilder();
        StringBuilder runErrorSb = new StringBuilder();

        try {
            Process runProcess = Runtime.getRuntime().exec(runCommand);

            runProcess.waitFor();

            InputStream inputStream = runProcess.getInputStream();

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String normalLine;

            while ((normalLine = bufferedReader.readLine()) != null) {
                runCompileNormalSb.append(normalLine).append("\n");
            }

            InputStream errorStream = runProcess.getErrorStream();

            errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));

            String errorLine;

            while ((errorLine = errorReader.readLine()) != null) {
                runErrorSb.append(errorLine).append("\n");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (errorReader != null) errorReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(compileNormalSb);
        System.out.println("---------------------------");
        System.out.println(errorSb);
        System.out.println("---------------------------");
        System.out.println(runCompileNormalSb);
        System.out.println("---------------------------");
        System.out.println(runErrorSb);
    }
}
