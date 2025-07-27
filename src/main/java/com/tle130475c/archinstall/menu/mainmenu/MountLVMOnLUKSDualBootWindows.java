package com.tle130475c.archinstall.menu.mainmenu;

import java.io.IOException;

import com.tle130475c.archinstall.partition.LVMOnLUKSDualBootWindows;

public class MountLVMOnLUKSDualBootWindows implements Runnable {
    @Override
    public void run() {
        System.console().printf("Enter disk name (e.g., sda, nvme0n1): ");
        String diskName = System.console().readLine().trim();

        System.console().printf("Enter LUKS password: ");
        String password = new String(System.console().readPassword());

        LVMOnLUKSDualBootWindows lvmOnLUKSDualBootWindows = new LVMOnLUKSDualBootWindows(diskName, null, null, password);
        try {
            lvmOnLUKSDualBootWindows.mount();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
