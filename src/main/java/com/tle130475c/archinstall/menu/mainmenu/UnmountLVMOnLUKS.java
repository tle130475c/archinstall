package com.tle130475c.archinstall.menu.mainmenu;

import java.io.IOException;

import com.tle130475c.archinstall.partition.LVMOnLUKS;

public class UnmountLVMOnLUKS implements Runnable {
    @Override
    public void run() {
        System.console().printf("Enter disk name (e.g., sda, nvme0n1): ");
        String diskName = System.console().readLine().trim();

        LVMOnLUKS lvmOnLUKS = new LVMOnLUKS(diskName, null, null, null, null);
        try {
            lvmOnLUKS.unmount();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
