package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.ConfigUtil.addUserToGroup;
import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.startService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class Docker implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public Docker(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("docker", "docker-compose", "docker-buildx", "minikube", "kubectl", "helm"),
                chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("docker.service", chrootDir);

        if (chrootDir == null) {
            startService("docker.service", chrootDir);
        }

        if (userAccount != null) {
            addUserToGroup(userAccount.getUsername(), "docker", chrootDir);
        }

        return 0;
    }
}
