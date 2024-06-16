package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.isAnswerYes;
import static com.tle130475c.archinstall.util.PackageUtil.installYayAURHelper;

import java.io.IOException;

import com.tle130475c.archinstall.systeminfo.UserAccount;

public class InstallYayAURHelper implements Runnable {
    @Override
    public void run() {
        System.console().printf("Username: ");
        final String username = System.console().readLine();
        UserAccount userAccount = new UserAccount(null, username, null);

        if (isAnswerYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                installYayAURHelper(userAccount, null);
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
