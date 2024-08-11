package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class Mullvad implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public Mullvad(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("mullvad-vpn-bin"), userAccount, chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("mullvad-daemon.service", chrootDir);
        return 0;
    }
}
