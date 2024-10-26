package com.tle130475c.archinstall.osinstall.virtualmachine;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class VMware implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public VMware(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("vmware-workstation"), userAccount, chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("vmware-networks", chrootDir);
        enableService("vmware-usbarbitrator", chrootDir);
        return 0;
    }
}
