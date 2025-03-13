package com.tle130475c.archinstall.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ShellUtil {
    private static final String COMMAND_EXECUTION_FAILURE_MESSAGE = "Failed to run command {}";
    private static final String ARCH_CHROOT_COMMAND = "arch-chroot";

    private ShellUtil() {
    }

    public static int runPipelineSilent(List<List<String>> commands) throws IOException, InterruptedException {
        List<ProcessBuilder> builders = commands.stream().map(ProcessBuilder::new).toList();
        List<Process> processes = ProcessBuilder.startPipeline(builders);
        processes.getLast().waitFor();

        int exitValue = processes.getLast().exitValue();
        if (exitValue != 0) {
            log.error(COMMAND_EXECUTION_FAILURE_MESSAGE, commands);
        }

        return exitValue;
    }

    public static int runSilent(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        process.waitFor();

        int exitValue = process.exitValue();
        logCommandExecutionStatus(exitValue, command);

        return exitValue;
    }

    public static int runVerbose(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).inheritIO().start();
        process.waitFor();

        int exitValue = process.exitValue();
        logCommandExecutionStatus(exitValue, command);

        return exitValue;
    }

    public static String runGetOutput(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes()).trim();
        process.waitFor();

        int exitValue = process.exitValue();
        logCommandExecutionStatus(exitValue, command);

        return output;
    }

    public static int runSetInput(List<String> command, List<String> inputList)
            throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();

        try (var ps = new PrintStream(process.getOutputStream(), false, Charset.defaultCharset())) {
            for (String input : inputList) {
                ps.println(input);
            }
        }

        process.waitFor();

        int exitValue = process.exitValue();
        logCommandExecutionStatus(exitValue, command);

        return exitValue;
    }

    public static int runAppendOutputToFile(List<String> command, String filePath)
            throws InterruptedException, IOException {
        Process process = new ProcessBuilder(command)
                .inheritIO()
                .redirectOutput(Redirect.appendTo(new File(filePath)))
                .start();
        process.waitFor();

        int exitValue = process.exitValue();
        logCommandExecutionStatus(exitValue, command);

        return exitValue;
    }

    public static List<String> getCommandRunSudo(List<String> command) {
        return Stream.concat(List.of("sudo").stream(), command.stream()).toList();
    }

    public static List<String> getCommandRunChroot(List<String> command, String chrootDir) {
        return Stream.concat(List.of(ARCH_CHROOT_COMMAND, chrootDir).stream(), command.stream()).toList();
    }

    public static List<String> getCommandRunChrootAsUser(List<String> command, String username, String chrootDir) {
        return Stream.concat(List.of(ARCH_CHROOT_COMMAND, "-u", username, chrootDir).stream(), command.stream())
                .toList();
    }

    private static void logCommandExecutionStatus(int exitValue, List<String> command) {
        if (exitValue != 0) {
            log.error(COMMAND_EXECUTION_FAILURE_MESSAGE, command);
        }
    }
}
