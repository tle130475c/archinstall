package com.tle130475c.archinstall.menu.mainmenu;

import com.tle130475c.archinstall.osinstall.virtualmachine.VirtualBox;
import com.tle130475c.archinstall.systeminfo.UserAccount;

import java.io.IOException;

import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;

public class InstallVirtualBox implements Runnable {
    @Override
    public void run() {
        System.console().printf("Username: ");
        final String username = System.console().readLine();
        UserAccount userAccount = new UserAccount(null, username, null);
        VirtualBox virtualBox = new VirtualBox(null, userAccount);

        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                virtualBox.install();
                virtualBox.config();
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
