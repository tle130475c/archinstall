package com.tle130475c.archinstall.util;

import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChrootAsUser;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunSudo;
import static com.tle130475c.archinstall.util.ShellUtil.runGetOutput;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConfigUtil {
    private static final String SYSTEMCTL = "systemctl";

    private ConfigUtil() {
    }

    public static int addUserToGroup(String username, String group, String chrootDir)
            throws InterruptedException, IOException {
        List<String> command = List.of("gpasswd", "-a", username, group);
        return runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));
    }

    public static void backupFile(String path) throws IOException {
        Files.copy(Paths.get(path), Paths.get(path + "_" + LocalDateTime.now()), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void backupFile(String path, String newPath) throws IOException {
        Files.copy(Paths.get(path), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void findAndReplaceInLine(String path, String linePattern, String target, String replacement)
            throws IOException {
        Pattern pattern = Pattern.compile(linePattern);
        List<String> lines = Files.readAllLines(Paths.get(path));

        int lineNumber = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (pattern.matcher(lines.get(i)).matches()) {
                lineNumber = i;
                break;
            }
        }

        if (lineNumber != -1) {
            lines.set(lineNumber, lines.get(lineNumber).replace(target, replacement));
            try (var writer = new PrintWriter(path)) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
        }
    }

    public static boolean isServiceStarted(String service, String chrootDir) throws IOException, InterruptedException {
        List<String> command = List.of(SYSTEMCTL, "show", "--property=ActiveEnterTimestamp", "--no-pager", service);
        String output = runGetOutput(
                chrootDir != null ? getCommandRunChroot(command, chrootDir) : command)
                .replace("ActiveEnterTimestamp=", "");

        return !output.isBlank();
    }

    public static String getServiceState(String service, String chrootDir) throws IOException, InterruptedException {
        List<String> command = List.of(SYSTEMCTL, "show", "--no-pager", "-p", "SubState", "--value", service);
        return runGetOutput(chrootDir != null ? getCommandRunChroot(command, chrootDir) : command);
    }

    public static int startService(String service, String chrootDir) throws InterruptedException, IOException {
        return manageSystemService("start", service, chrootDir);
    }

    public static int stopService(String service, String chrootDir) throws InterruptedException, IOException {
        return manageSystemService("stop", service, chrootDir);
    }

    public static int enableService(String service, String chrootDir) throws InterruptedException, IOException {
        return manageSystemService("enable", service, chrootDir);
    }

    public static int disableService(String service, String chrootDir) throws InterruptedException, IOException {
        return manageSystemService("disable", service, chrootDir);
    }

    public static int restartService(String service, String chrootDir) throws InterruptedException, IOException {
        return manageSystemService("restart", service, chrootDir);
    }

    public static int maskService(String service, String chrootDir) throws InterruptedException, IOException {
        return manageSystemService("mask", service, chrootDir);
    }

    public static void uncommentLine(String path, String linePattern) throws IOException {
        backupFile(path);

        List<String> lines = Files.readAllLines(Paths.get(path));
        int lineNumber = lines.indexOf(linePattern);

        if (lineNumber != -1) {
            lines.set(lineNumber, lines.get(lineNumber).replace("#", "").trim());
            try (var writer = new PrintWriter(path)) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
        }
    }

    public static void commentLine(String path, String linePattern) throws IOException {
        backupFile(path);

        List<String> lines = Files.readAllLines(Paths.get(path));
        int lineNumber = lines.indexOf(linePattern);

        if (lineNumber != -1) {
            lines.set(lineNumber, "# " + lines.get(lineNumber));
            try (var writer = new PrintWriter(path)) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
        }
    }

    public static boolean deleteMatchingFiles(String dir, String regexPattern) throws IOException {
        Path directory = Paths.get(dir);
        Pattern pattern = Pattern.compile(regexPattern);
        AtomicBoolean fileDeleted = new AtomicBoolean(false); // Track if any file was deleted

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pattern.matcher(file.getFileName().toString()).matches()) {
                    log.info("Deleting: " + file);
                    Files.delete(file);
                    fileDeleted.set(true);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return fileDeleted.get();
    }

    public static void cleanUpGnuPGLock(String gnuPGConfigPath) throws IOException {
        deleteMatchingFiles(gnuPGConfigPath, "^\\.\\#lk.*");
        deleteMatchingFiles(gnuPGConfigPath, "^pubring\\.db\\.lock$");
    }

    public static void createUserEnvironmentDir(String username, String chrootDir)
            throws IOException, InterruptedException {
        List<String> command = List.of("mkdir", "-p", "/home/%s/.config/environment.d".formatted(username));
        runVerbose(chrootDir != null
                ? getCommandRunChrootAsUser(command, username, chrootDir)
                : command);
    }

    private static int manageSystemService(String action, String service, String chrootDir)
            throws InterruptedException, IOException {
        List<String> command = List.of(SYSTEMCTL, action, service);
        return runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));
    }
}
