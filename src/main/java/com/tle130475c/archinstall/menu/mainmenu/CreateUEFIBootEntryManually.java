package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunSudo;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

public class CreateUEFIBootEntryManually implements Runnable {
    @Override
    public void run() {
        System.console().printf("Enter disk name (e.g. sda, nvme0n1,...): ");
        String diskName = System.console().readLine().trim();

        String chrootDir = null;
        if (confirmDefaultYes(getConfirmation(":: Is in live system? [Y/n] "))) {
            chrootDir = "/mnt";
        }

        List<String> command = List.of("efibootmgr", "--create",
                "--disk", "/dev/%s".formatted(diskName),
                "--part", "1",
                "--loader", "'\\EFI\\systemd\\systemd-bootx64.efi'",
                "--label", "archlinux",
                "--unicode");

        try {
            if (chrootDir != null) {
                runVerbose(getCommandRunChroot(command, chrootDir));
            } else {
                runVerbose(getCommandRunSudo(command));
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
