package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class Office implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public Office(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("libreoffice-fresh", "calibre", "kchmviewer", "foliate", "kolourpaint",
                "teams-for-linux", "telegram-desktop", "evolution", "evolution-ews",
                "evolution-on"), userAccount, chrootDir);

        return 0;
    }
}
