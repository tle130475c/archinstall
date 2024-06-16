package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunSudo;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class DiskImageTool implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public DiskImageTool(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("cdrtools", "cdemu-client", "vhba-module-dkms", "gcdemu"), userAccount, chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        List<String> loadDriversCommand = List.of("modprobe", "-a", "sg", "sr_mod", "vhba");

        runVerbose(chrootDir != null
                ? getCommandRunChroot(loadDriversCommand, chrootDir)
                : getCommandRunSudo(loadDriversCommand));

        return 0;
    }
}
