package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class CoreProgrammingTool implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public CoreProgrammingTool(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("git", "github-cli", "kdiff3", "valgrind", "kruler", "sublime-merge", "sublime-text",
                "visual-studio-code-bin", "postman-bin", "emacs-wayland", "gvim", "bash-language-server",
                "kate", "dbeaver", "dbeaver-plugin-office", "dbeaver-plugin-svg-format", "powershell-bin",
                "azure-cli"), userAccount, chrootDir);

        return 0;
    }
}
