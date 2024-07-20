package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.createEncryptedPartitionUsingLUKS;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.readPassword;
import static com.tle130475c.archinstall.util.IOUtil.readUsername;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

public class CreateEncryptedPartitionUsingLUKS implements Runnable {

    @Override
    public void run() {
        try {
            runVerbose(List.of("lsblk"));

            String username = readUsername("Enter username: ");

            System.console().printf("Enter disk name: ");
            String diskName = System.console().readLine();

            System.console().printf("Enter partition number: ");
            int partitionNumber = Integer.parseInt(System.console().readLine());

            System.console().printf("Enter mapper name: ");
            String mapperName = System.console().readLine();

            String password = readPassword(
                    "Enter LUKS password: ",
                    "Re-enter LUKS password: ");

            if (confirmDefaultYes(getConfirmation(":: Proceed with encryption? [Y/n] "))) {
                createEncryptedPartitionUsingLUKS(diskName, partitionNumber, mapperName, password, username);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
