package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;

import java.io.IOException;

import com.tle130475c.archinstall.osinstall.desktopenvironment.KDEPlasma;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class InstallFcitx5Bamboo implements Runnable {
    @Override
    public void run() {
        System.console().printf("Username: ");
        final String username = System.console().readLine();
        UserAccount userAccount = new UserAccount(null, username, null);
        KDEPlasma kdePlasma = new KDEPlasma(null, userAccount);

        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                kdePlasma.installFcitx5Bamboo();
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
