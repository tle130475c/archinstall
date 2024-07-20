package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.createLinuxLUKSPartition;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

public class CreateLinuxLUKSPartition implements Runnable {
    @Override
    public void run() {
        try {
            runVerbose(List.of("lsblk"));

            System.console().printf("Enter disk name: ");
            String diskName = System.console().readLine();

            if (confirmDefaultYes(
                    getConfirmation(":: Proceed with create Linux LUKS partition on %s? [Y/n] ".formatted(diskName)))) {
                createLinuxLUKSPartition(diskName, 0, null);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
