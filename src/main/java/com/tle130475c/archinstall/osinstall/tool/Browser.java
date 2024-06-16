package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class Browser implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public Browser(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("torbrowser-launcher", "firefox-developer-edition", "google-chrome"),
                userAccount, chrootDir);

        return 0;
    }
}
