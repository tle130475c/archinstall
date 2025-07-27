package com.tle130475c.archinstall.menu.mainmenu;

import java.io.IOException;

import com.tle130475c.archinstall.partition.LVMOnLUKSDualBootWindows;

public class UnmountLVMOnLUKSDualBootWindows implements Runnable {
    @Override
    public void run() {
        System.console().printf("Enter disk name (e.g., sda, nvme0n1): ");
        String diskName = System.console().readLine().trim();

        LVMOnLUKSDualBootWindows lvmOnLUKSDualBootWindows = new LVMOnLUKSDualBootWindows(diskName, null, null, null);
        try {
            lvmOnLUKSDualBootWindows.unmount();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
