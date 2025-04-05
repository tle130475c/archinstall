package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChrootAsUser;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class CodeEditor implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public CodeEditor(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        return installPkgs(List.of("visual-studio-code-bin", "visual-studio-code-insiders-bin"), userAccount,
                chrootDir);
    }

    @Override
    public int config() throws IOException, InterruptedException {
        installExtensions();
        copyUserSettings("/home/%s/.config/Code/User/settings.json".formatted(userAccount.getUsername()));
        copyUserSettings("/home/%s/.config/Code - Insiders/User/settings.json".formatted(userAccount.getUsername()));

        return 0;
    }

    private void copyUserSettings(String settingsPath) throws IOException, InterruptedException {
        try (var inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("vscodeconfig/settings.json")) {
            Files.copy(inputStream, Paths.get(chrootDir + settingsPath), StandardCopyOption.REPLACE_EXISTING);

            // Change ownership of the copied files to the user
            if (chrootDir != null) {
                runVerbose(getCommandRunChroot(List.of("chown",
                        "%s:%s".formatted(userAccount.getUsername(), userAccount.getUsername()), settingsPath),
                        chrootDir));
            }
        }
    }

    private void installExtensions() {
        try (var reader = new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("vscodeconfig/extensions.txt"))) {
            Stream<String> extensions = new BufferedReader(reader).lines();
            extensions.forEach(extension -> {
                try {
                    runVerbose(getExtensionInstallCommand(extension));
                    runVerbose(getInsiderExtensionInstallCommand(extension));
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getExtensionInstallCommand(String extension) {
        if (chrootDir == null) {
            return List.of("code", "--install-extension", extension);
        } else {
            String cmd = "code --install-extension %s".formatted(extension);
            return getCommandRunChrootAsUser(cmd, userAccount.getUsername(), chrootDir);
        }
    }

    private List<String> getInsiderExtensionInstallCommand(String extension) {
        if (chrootDir == null) {
            return List.of("code-insiders", "--install-extension", extension, "--pre-release");
        } else {
            String cmd = "code-insiders --install-extension %s --pre-release".formatted(extension);
            return getCommandRunChrootAsUser(cmd, userAccount.getUsername(), chrootDir);
        }
    }
}
