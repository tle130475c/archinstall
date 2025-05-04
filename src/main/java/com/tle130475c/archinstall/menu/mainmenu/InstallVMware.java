package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;

import java.io.IOException;

import com.tle130475c.archinstall.osinstall.virtualmachine.VMware;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class InstallVMware implements Runnable {
    @Override
    public void run() {
        System.console().printf("Username: ");
        final String username = System.console().readLine();
        UserAccount userAccount = new UserAccount(null, username, null);
        VMware vmware = new VMware(null, userAccount);

        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                vmware.install();
                vmware.config();
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
