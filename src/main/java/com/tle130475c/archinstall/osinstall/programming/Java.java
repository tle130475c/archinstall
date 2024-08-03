package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

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
        installPkgs(List.of("jdk-openjdk", "openjdk-doc", "openjdk-src", "jdk11-openjdk", "maven", "gradle",
                "gradle-doc", "xorg-fonts-type1", "jdk21-openjdk", "jdk17-openjdk"), userAccount, chrootDir);

        return 0;
    }
}
