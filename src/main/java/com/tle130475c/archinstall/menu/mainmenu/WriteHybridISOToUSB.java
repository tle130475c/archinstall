package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.createPartition;
import static com.tle130475c.archinstall.util.DiskUtil.eraseDisk;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.tle130475c.archinstall.partition.Partition;

public class WriteHybridISOToUSB implements Runnable {
    @Override
    public void run() {
        try {
            runVerbose(List.of("lsblk"));
            System.console().printf("Enter USB's name(e.g. sdb, sdc,...): ");
            final String usbName = System.console().readLine();

            System.console().printf("Enter full path to the hybrid ISO: ");
            final String isoPath = System.console().readLine();

            if (!Files.exists(Paths.get(isoPath)) || Files.isDirectory(Paths.get(isoPath))) {
                System.console().printf("File not found!%n");
                return;
            }

            Partition usbPartition = new Partition(usbName, "8309", "boot-usb");
            eraseDisk(usbPartition.getPathToDisk());
            createPartition(usbPartition);
            runVerbose(List.of("dd", "if=%s".formatted(isoPath),
                    "of=%s".formatted(usbPartition.getPathToDisk()), "bs=4M", "conv=sync", "status=progress"));

            Thread.sleep(30000);
            runVerbose(List.of("udisksctl", "power-off", "-b", usbPartition.getPathToDisk()));
            System.console().printf("Succcessfully wrote %s to %s%n".formatted(isoPath, usbPartition.getPathToDisk()));
            System.console().printf("Please unplug the USB!%n");
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
