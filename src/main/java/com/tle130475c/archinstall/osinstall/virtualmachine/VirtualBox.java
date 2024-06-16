package com.tle130475c.archinstall.osinstall.virtualmachine;

import static com.tle130475c.archinstall.util.ConfigUtil.addUserToGroup;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class VirtualBox implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public VirtualBox(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("virtualbox", "virtualbox-guest-iso", "virtualbox-host-dkms", "virtualbox-ext-oracle"),
                userAccount, chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        addUserToGroup(userAccount.getUsername(), "vboxusers", chrootDir);
        return 0;
    }

}
