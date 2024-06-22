package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.eraseDisk;
import static com.tle130475c.archinstall.util.DiskUtil.getPathToDisk;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

public class EraseDisk implements Runnable {
    @Override
    public void run() {
        try {
            runVerbose(List.of("lsblk"));

            System.console().printf("Enter disk's name (e.g. nvme0n1, sda): ");
            String diskName = System.console().readLine();

            if (confirmDefaultYes(getConfirmation(":: Proceed with erase? [Y/n] "))) {
                eraseDisk(getPathToDisk(diskName));
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
