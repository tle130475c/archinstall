package com.tle130475c.archinstall.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

public final class ShellUtil {
    private static final String ARCH_CHROOT_COMMAND = "arch-chroot";

    private ShellUtil() {
    }

    public static int runPipelineSilent(List<List<String>> commands) throws IOException, InterruptedException {
        List<ProcessBuilder> builders = commands.stream().map(ProcessBuilder::new).toList();
        List<Process> processes = ProcessBuilder.startPipeline(builders);
        processes.getLast().waitFor();

        return processes.getLast().exitValue();
    }

    public static int runSilent(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        process.waitFor();

        return process.exitValue();
    }

    public static int runVerbose(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).inheritIO().start();
        process.waitFor();

        return process.exitValue();
    }

    public static String runGetOutput(List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        return new String(builder.start().getInputStream().readAllBytes()).trim();
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

        return process.exitValue();
    }

    public static int runAppendOutputToFile(List<String> command, String filePath)
            throws InterruptedException, IOException {
        return new ProcessBuilder(command)
                .inheritIO()
                .redirectOutput(Redirect.appendTo(new File(filePath)))
                .start().waitFor();
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
}
