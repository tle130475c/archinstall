package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;

import java.io.IOException;

public class InstallFlatpakPackages implements Runnable {
    @Override
    public void run() {
        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                ConfigureSystem.installFlatpakPkgs();
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
