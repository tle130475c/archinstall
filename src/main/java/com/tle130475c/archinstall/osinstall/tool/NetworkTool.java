package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class NetworkTool implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public NetworkTool(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("nmap", "wireshark-qt", "wireshark-cli", "proton-vpn-gtk-app"), userAccount, chrootDir);
        return 0;
    }
}
