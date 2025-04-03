package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunSudo;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class Java implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public Java(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("jdk-openjdk", "openjdk-doc", "openjdk-src", "maven", "gradle",
                "gradle-doc", "xorg-fonts-type1", "jdk21-openjdk", "jetbrains-toolbox"),
                userAccount, chrootDir);

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        List<String> command = List.of("archlinux-java", "set", "java-21-openjdk");
        return runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));
    }
}
