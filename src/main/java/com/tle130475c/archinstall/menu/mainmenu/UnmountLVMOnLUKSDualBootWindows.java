package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.partition.LVMOnLUKSDualBootWindows;

public class UnmountLVMOnLUKSDualBootWindows implements Runnable {
    @Override
    public void run() {
        try {
            runVerbose(List.of("lsblk"));

            System.console().printf("Enter disk name (e.g., sda, nvme0n1): ");
            String diskName = System.console().readLine().trim();

            System.console().printf("Enter XBOOTLDR partition number: ");
            int xbootldrPartNumber = Integer.parseInt(System.console().readLine().trim());

            System.console().printf("Enter Linux LUKS partition number: ");
            int linuxLUKSPartNumber = Integer.parseInt(System.console().readLine().trim());

            LVMOnLUKSDualBootWindows lvmOnLUKSDualBootWindows = new LVMOnLUKSDualBootWindows(diskName, null, null, null,
                    xbootldrPartNumber, linuxLUKSPartNumber);
            lvmOnLUKSDualBootWindows.unmount();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
