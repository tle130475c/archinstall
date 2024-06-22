package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgsWithOptionalDeps;

import java.io.IOException;
import java.util.List;

public class InstallLutris implements Runnable {
    @Override
    public void run() {
        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                installMainReposPkgsWithOptionalDeps(List.of("lutris", "wine", "winetricks"), null);
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
