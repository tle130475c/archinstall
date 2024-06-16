package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.encryptDiskUsingLUKS;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.isAnswerYes;
import static com.tle130475c.archinstall.util.IOUtil.readPassword;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

public class EncryptDisk implements Runnable {
    @Override
    public void run() {
        try {
            runVerbose(List.of("lsblk"));

            System.console().printf("Enter username: ");
            String username = System.console().readLine();

            System.console().printf("Enter disk name: ");
            String diskName = System.console().readLine();

            System.console().printf("Enter mapper name: ");
            String mapperName = System.console().readLine();

            String password = readPassword(
                    "Enter LUKS password: ",
                    "Re-enter LUKS password: ");

            if (isAnswerYes(getConfirmation(":: Proceed with encryption? [Y/n] "))) {
                encryptDiskUsingLUKS(diskName, mapperName, password, username);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
